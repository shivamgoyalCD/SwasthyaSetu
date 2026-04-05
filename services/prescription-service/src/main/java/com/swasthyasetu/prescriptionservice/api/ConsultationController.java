package com.swasthyasetu.prescriptionservice.api;

import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import com.swasthyasetu.prescriptionservice.domain.Consultation;
import com.swasthyasetu.prescriptionservice.dto.ConsultationSummaryResponse;
import com.swasthyasetu.prescriptionservice.dto.GeneratePrescriptionResponse;
import com.swasthyasetu.prescriptionservice.dto.LlmSummaryResponse;
import com.swasthyasetu.prescriptionservice.dto.UpdateConsultationSummaryRequest;
import com.swasthyasetu.prescriptionservice.security.CurrentUser;
import com.swasthyasetu.prescriptionservice.security.CurrentUserService;
import com.swasthyasetu.prescriptionservice.service.ChatServiceException;
import com.swasthyasetu.prescriptionservice.service.ConsultationService;
import com.swasthyasetu.prescriptionservice.service.PrescriptionGenerationException;
import com.swasthyasetu.prescriptionservice.service.PrescriptionService;
import com.swasthyasetu.prescriptionservice.service.SummaryGenerationException;
import com.swasthyasetu.prescriptionservice.service.SummaryNotFoundException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConsultationController {
  private final ConsultationService consultationService;
  private final PrescriptionService prescriptionService;
  private final CurrentUserService currentUserService;

  @PostMapping("/prescriptions/consultations/start")
  public ResponseEntity<ApiResponse<Consultation>> startConsultation(
      @RequestParam("appointmentId") String appointmentId
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"PATIENT".equals(currentUser.get().getRole()) && !"DOCTOR".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Patient or doctor role required", null)
      ));
    }

    if (appointmentId == null || appointmentId.trim().isEmpty()) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "appointmentId is required", null)
      ));
    }

    UUID parsedAppointmentId;
    try {
      parsedAppointmentId = UUID.fromString(appointmentId);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_APPOINTMENT_ID", "appointmentId must be a valid UUID", null)
      ));
    }

    Consultation consultation = consultationService.startConsultation(parsedAppointmentId);
    return ResponseEntity.ok(new ApiResponse<>(true, consultation, null));
  }

  @PostMapping("/prescriptions/consultations/end")
  public ResponseEntity<ApiResponse<ConsultationSummaryResponse>> endConsultation(
      @RequestParam("consultationId") String consultationId,
      @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"DOCTOR".equals(currentUser.get().getRole()) && !"ADMIN".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctor or admin role required", null)
      ));
    }

    if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authorization header required", null)
      ));
    }

    if (consultationId == null || consultationId.trim().isEmpty()) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "consultationId is required", null)
      ));
    }

    UUID parsedConsultationId;
    try {
      parsedConsultationId = UUID.fromString(consultationId);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_CONSULTATION_ID", "consultationId must be a valid UUID", null)
      ));
    }

    try {
      return consultationService.endConsultation(parsedConsultationId, authorizationHeader)
          .map(summary -> ResponseEntity.ok(new ApiResponse<>(true, summary, null)))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
              false,
              null,
              new ApiError("CONSULTATION_NOT_FOUND", "Consultation not found", null)
          )));
    } catch (ChatServiceException ex) {
      return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ApiResponse<>(
          false,
          null,
          new ApiError("CHAT_SERVICE_ERROR", "Failed to fetch chat messages for summary generation", null)
      ));
    } catch (SummaryGenerationException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
          false,
          null,
          new ApiError("SUMMARY_GENERATION_FAILED", "Failed to generate consultation summary", null)
      ));
    }
  }

  @PatchMapping("/prescriptions/consultations/{id}/summary")
  public ResponseEntity<ApiResponse<LlmSummaryResponse>> updateSummary(
      @PathVariable("id") String id,
      @RequestBody UpdateConsultationSummaryRequest request
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"DOCTOR".equals(currentUser.get().getRole()) && !"ADMIN".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctor or admin role required", null)
      ));
    }

    if (request == null || request.getJsonSummary() == null) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "json_summary is required", null)
      ));
    }

    UUID consultationId;
    try {
      consultationId = UUID.fromString(id);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_CONSULTATION_ID", "id must be a valid UUID", null)
      ));
    }

    return consultationService.updateSummary(consultationId, request.getJsonSummary())
        .map(summary -> ResponseEntity.ok(new ApiResponse<>(true, LlmSummaryResponse.fromEntity(summary), null)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
            false,
            null,
            new ApiError("SUMMARY_NOT_FOUND", "Summary not found for consultation", null)
        )));
  }

  @PostMapping("/prescriptions/consultations/{id}/prescription/generate")
  public ResponseEntity<ApiResponse<GeneratePrescriptionResponse>> generatePrescription(
      @PathVariable("id") String id
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"DOCTOR".equals(currentUser.get().getRole()) && !"ADMIN".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctor or admin role required", null)
      ));
    }

    UUID consultationId;
    try {
      consultationId = UUID.fromString(id);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_CONSULTATION_ID", "id must be a valid UUID", null)
      ));
    }

    try {
      return prescriptionService.generatePrescription(consultationId)
          .map(response -> ResponseEntity.ok(new ApiResponse<>(true, response, null)))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
              false,
              null,
              new ApiError("CONSULTATION_NOT_FOUND", "Consultation not found", null)
          )));
    } catch (SummaryNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
          false,
          null,
          new ApiError("SUMMARY_NOT_FOUND", "Summary not found for consultation", null)
      ));
    } catch (PrescriptionGenerationException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
          false,
          null,
          new ApiError("PRESCRIPTION_GENERATION_FAILED", "Failed to generate prescription PDF", null)
      ));
    }
  }
}
