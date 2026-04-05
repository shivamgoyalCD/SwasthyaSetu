package com.swasthyasetu.prescriptionservice.api;

import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import com.swasthyasetu.prescriptionservice.dto.PrescriptionDownloadResponse;
import com.swasthyasetu.prescriptionservice.security.CurrentUser;
import com.swasthyasetu.prescriptionservice.security.CurrentUserService;
import com.swasthyasetu.prescriptionservice.service.PrescriptionDownloadException;
import com.swasthyasetu.prescriptionservice.service.PrescriptionService;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PrescriptionController {
  private final PrescriptionService prescriptionService;
  private final CurrentUserService currentUserService;

  @GetMapping("/prescriptions/{id}/download")
  public ResponseEntity<ApiResponse<PrescriptionDownloadResponse>> download(@PathVariable("id") String id) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"PATIENT".equals(currentUser.get().getRole())
        && !"DOCTOR".equals(currentUser.get().getRole())
        && !"ADMIN".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Patient, doctor or admin role required", null)
      ));
    }

    UUID prescriptionId;
    try {
      prescriptionId = UUID.fromString(id);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_PRESCRIPTION_ID", "id must be a valid UUID", null)
      ));
    }

    try {
      return prescriptionService.getDownloadUrl(prescriptionId)
          .map(response -> ResponseEntity.ok(new ApiResponse<>(true, response, null)))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
              false,
              null,
              new ApiError("PRESCRIPTION_NOT_FOUND", "Prescription not found", null)
          )));
    } catch (PrescriptionDownloadException ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
          false,
          null,
          new ApiError("PRESCRIPTION_DOWNLOAD_FAILED", "Failed to generate prescription download URL", null)
      ));
    }
  }
}
