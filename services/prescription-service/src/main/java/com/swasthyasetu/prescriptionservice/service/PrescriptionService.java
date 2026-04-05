package com.swasthyasetu.prescriptionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swasthyasetu.prescriptionservice.domain.Consultation;
import com.swasthyasetu.prescriptionservice.domain.LlmSummary;
import com.swasthyasetu.prescriptionservice.domain.Prescription;
import com.swasthyasetu.prescriptionservice.dto.GeneratePrescriptionResponse;
import com.swasthyasetu.prescriptionservice.dto.PrescriptionDownloadResponse;
import com.swasthyasetu.prescriptionservice.repository.ConsultationRepository;
import com.swasthyasetu.prescriptionservice.repository.LlmSummaryRepository;
import com.swasthyasetu.prescriptionservice.repository.PrescriptionRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrescriptionService {
  private static final PDFont TITLE_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private static final PDFont LABEL_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
  private static final PDFont BODY_FONT = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
  private static final float PAGE_MARGIN = 56f;
  private static final float PAGE_WIDTH = PDRectangle.A4.getWidth() - (PAGE_MARGIN * 2);
  private static final float TITLE_FONT_SIZE = 18f;
  private static final float LABEL_FONT_SIZE = 12f;
  private static final float BODY_FONT_SIZE = 11f;
  private static final float LINE_HEIGHT = 16f;

  private final ConsultationRepository consultationRepository;
  private final LlmSummaryRepository llmSummaryRepository;
  private final PrescriptionRepository prescriptionRepository;
  private final MinioClient minioClient;
  private final ObjectMapper objectMapper;

  @Value("${minio.bucket}")
  private String minioBucket;

  @Transactional
  public Optional<GeneratePrescriptionResponse> generatePrescription(UUID consultationId) {
    return consultationRepository.findById(consultationId)
        .map(this::generatePrescription);
  }

  @Transactional(readOnly = true)
  public Optional<PrescriptionDownloadResponse> getDownloadUrl(UUID prescriptionId) {
    return prescriptionRepository.findById(prescriptionId)
        .map(this::toDownloadResponse);
  }

  private GeneratePrescriptionResponse generatePrescription(Consultation consultation) {
    LlmSummary summary = llmSummaryRepository.findByConsultationId(consultation.getId())
        .orElseThrow(() -> new SummaryNotFoundException("Summary not found for consultation"));

    SummaryFields fields = parseSummary(summary.getJsonSummary());
    byte[] pdfBytes = buildPdf(consultation, fields);
    String objectKey = "prescriptions/" + consultation.getId() + ".pdf";
    uploadPdf(objectKey, pdfBytes);

    Prescription prescription = prescriptionRepository.findByConsultationId(consultation.getId())
        .orElseGet(() -> new Prescription(
            UUID.randomUUID(),
            consultation.getId(),
            objectKey,
            LocalDateTime.now()
        ));
    prescription.setPdfS3Key(objectKey);
    if (prescription.getCreatedAt() == null) {
      prescription.setCreatedAt(LocalDateTime.now());
    }

    Prescription saved = prescriptionRepository.save(prescription);
    return new GeneratePrescriptionResponse(saved.getId());
  }

  private PrescriptionDownloadResponse toDownloadResponse(Prescription prescription) {
    if (prescription.getPdfS3Key() == null || prescription.getPdfS3Key().isBlank()) {
      throw new PrescriptionDownloadException("Prescription PDF key is missing");
    }

    try {
      String downloadUrl = minioClient.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(minioBucket)
              .object(prescription.getPdfS3Key())
              .expiry(15, TimeUnit.MINUTES)
              .build()
      );
      return new PrescriptionDownloadResponse(downloadUrl);
    } catch (Exception ex) {
      throw new PrescriptionDownloadException("Failed to generate prescription download URL", ex);
    }
  }

  private SummaryFields parseSummary(String jsonSummary) {
    if (jsonSummary == null || jsonSummary.isBlank()) {
      return new SummaryFields("", "", "");
    }

    try {
      JsonNode root = objectMapper.readTree(jsonSummary);
      return new SummaryFields(
          readText(root, "complaint"),
          readText(root, "advice"),
          readText(root, "notes")
      );
    } catch (Exception ex) {
      return new SummaryFields("", "", jsonSummary);
    }
  }

  private String readText(JsonNode root, String fieldName) {
    JsonNode node = root.get(fieldName);
    if (node == null || node.isNull()) {
      return "";
    }
    return node.isTextual() ? node.asText() : node.toString();
  }

  private byte[] buildPdf(Consultation consultation, SummaryFields fields) {
    try (PDDocument document = new PDDocument();
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      PDPage page = new PDPage(PDRectangle.A4);
      document.addPage(page);

      try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        float y = page.getMediaBox().getHeight() - PAGE_MARGIN;
        y = writeLine(contentStream, "Prescription", TITLE_FONT, TITLE_FONT_SIZE, y);
        y -= 8f;
        y = writeField(contentStream, "Consultation ID", consultation.getId().toString(), y);
        y = writeField(contentStream, "Appointment ID", consultation.getAppointmentId().toString(), y);
        y = writeField(contentStream, "Complaint", fields.complaint(), y);
        y = writeField(contentStream, "Advice", fields.advice(), y);
        writeField(contentStream, "Notes", fields.notes(), y);
      }

      document.save(outputStream);
      return outputStream.toByteArray();
    } catch (IOException ex) {
      throw new PrescriptionGenerationException("Failed to build prescription PDF", ex);
    }
  }

  private void uploadPdf(String objectKey, byte[] pdfBytes) {
    try {
      minioClient.putObject(
          PutObjectArgs.builder()
              .bucket(minioBucket)
              .object(objectKey)
              .stream(new ByteArrayInputStream(pdfBytes), pdfBytes.length, -1)
              .contentType("application/pdf")
              .build()
      );
    } catch (Exception ex) {
      throw new PrescriptionGenerationException("Failed to upload prescription PDF", ex);
    }
  }

  private float writeField(PDPageContentStream contentStream, String label, String value, float y) throws IOException {
    y = writeLine(contentStream, label + ":", LABEL_FONT, LABEL_FONT_SIZE, y);
    List<String> lines = wrapText(value == null ? "" : value, BODY_FONT, BODY_FONT_SIZE, PAGE_WIDTH);
    if (lines.isEmpty()) {
      lines = List.of("");
    }
    for (String line : lines) {
      y = writeLine(contentStream, line, BODY_FONT, BODY_FONT_SIZE, y);
    }
    return y - 6f;
  }

  private float writeLine(PDPageContentStream contentStream, String text, PDFont font, float fontSize, float y)
      throws IOException {
    contentStream.beginText();
    contentStream.setFont(font, fontSize);
    contentStream.newLineAtOffset(PAGE_MARGIN, y);
    contentStream.showText(text == null ? "" : text);
    contentStream.endText();
    return y - LINE_HEIGHT;
  }

  private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
    List<String> lines = new ArrayList<>();
    if (text == null || text.isBlank()) {
      return lines;
    }

    for (String paragraph : text.replace("\r", "").split("\n")) {
      if (paragraph.isBlank()) {
        lines.add("");
        continue;
      }

      StringBuilder currentLine = new StringBuilder();
      for (String word : paragraph.split("\\s+")) {
        String candidate = currentLine.isEmpty() ? word : currentLine + " " + word;
        if (calculateWidth(candidate, font, fontSize) <= maxWidth) {
          currentLine.setLength(0);
          currentLine.append(candidate);
          continue;
        }

        if (!currentLine.isEmpty()) {
          lines.add(currentLine.toString());
          currentLine.setLength(0);
          currentLine.append(word);
        } else {
          lines.add(word);
        }
      }

      if (!currentLine.isEmpty()) {
        lines.add(currentLine.toString());
      }
    }

    return lines;
  }

  private float calculateWidth(String text, PDFont font, float fontSize) throws IOException {
    return font.getStringWidth(text) / 1000f * fontSize;
  }

  private record SummaryFields(
      String complaint,
      String advice,
      String notes
  ) {
  }
}
