package com.swasthyasetu.prescriptionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swasthyasetu.prescriptionservice.domain.Consultation;
import com.swasthyasetu.prescriptionservice.domain.LlmSummary;
import com.swasthyasetu.prescriptionservice.dto.ConsultationSummaryResponse;
import com.swasthyasetu.prescriptionservice.repository.ConsultationRepository;
import com.swasthyasetu.prescriptionservice.repository.LlmSummaryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultationService {
  private static final String STARTED_STATUS = "STARTED";
  private static final String ENDED_STATUS = "ENDED";

  private final ConsultationRepository consultationRepository;
  private final LlmSummaryRepository llmSummaryRepository;
  private final ChatServiceClient chatServiceClient;
  private final ObjectMapper objectMapper;

  @Transactional
  public Consultation startConsultation(UUID appointmentId) {
    Optional<Consultation> existing = consultationRepository.findByAppointmentId(appointmentId);
    if (existing.isPresent()) {
      return populateStartDefaults(existing.get());
    }

    LocalDateTime now = LocalDateTime.now();
    Consultation consultation = new Consultation(
        UUID.randomUUID(),
        appointmentId,
        STARTED_STATUS,
        now,
        null,
        now
    );

    try {
      return consultationRepository.saveAndFlush(consultation);
    } catch (DataIntegrityViolationException ex) {
      return consultationRepository.findByAppointmentId(appointmentId)
          .map(this::populateStartDefaults)
          .orElseThrow(() -> ex);
    }
  }

  private Consultation populateStartDefaults(Consultation consultation) {
    boolean changed = false;
    LocalDateTime now = LocalDateTime.now();

    if (consultation.getStatus() == null || consultation.getStatus().isBlank()) {
      consultation.setStatus(STARTED_STATUS);
      changed = true;
    }
    if (consultation.getStartedAt() == null) {
      consultation.setStartedAt(now);
      changed = true;
    }
    if (consultation.getCreatedAt() == null) {
      consultation.setCreatedAt(now);
      changed = true;
    }

    return changed ? consultationRepository.save(consultation) : consultation;
  }

  @Transactional
  public Optional<ConsultationSummaryResponse> endConsultation(UUID consultationId, String authorizationHeader) {
    return consultationRepository.findById(consultationId)
        .map(consultation -> endConsultation(consultation, authorizationHeader));
  }

  @Transactional
  public Optional<LlmSummary> updateSummary(UUID consultationId, String jsonSummary) {
    return llmSummaryRepository.findByConsultationId(consultationId)
        .map(summary -> {
          summary.setJsonSummary(jsonSummary);
          return llmSummaryRepository.save(summary);
        });
  }

  private ConsultationSummaryResponse endConsultation(Consultation consultation, String authorizationHeader) {
    List<ChatServiceClient.ChatMessagePayload> messages = chatServiceClient.getAppointmentMessages(
        consultation.getAppointmentId(),
        authorizationHeader
    );
    ConsultationSummaryResponse summary = new ConsultationSummaryResponse(
        "",
        "",
        concatenateMessages(messages)
    );

    if (!ENDED_STATUS.equals(consultation.getStatus())) {
      consultation.setStatus(ENDED_STATUS);
    }
    if (consultation.getEndedAt() == null) {
      consultation.setEndedAt(LocalDateTime.now());
    }
    consultationRepository.save(consultation);

    LlmSummary storedSummary = llmSummaryRepository.findByConsultationId(consultation.getId())
        .orElseGet(() -> new LlmSummary(
            UUID.randomUUID(),
            consultation.getId(),
            null,
            LocalDateTime.now()
        ));
    storedSummary.setJsonSummary(serializeSummary(summary));
    if (storedSummary.getCreatedAt() == null) {
      storedSummary.setCreatedAt(LocalDateTime.now());
    }
    llmSummaryRepository.save(storedSummary);

    return summary;
  }

  private String concatenateMessages(List<ChatServiceClient.ChatMessagePayload> messages) {
    return messages.stream()
        .map(ChatServiceClient.ChatMessagePayload::content)
        .filter(content -> content != null && !content.isBlank())
        .collect(Collectors.joining("\n"));
  }

  private String serializeSummary(ConsultationSummaryResponse summary) {
    try {
      return objectMapper.writeValueAsString(summary);
    } catch (JsonProcessingException ex) {
      throw new SummaryGenerationException("Failed to serialize consultation summary", ex);
    }
  }
}
