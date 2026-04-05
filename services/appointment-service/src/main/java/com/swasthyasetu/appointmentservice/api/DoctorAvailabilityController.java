package com.swasthyasetu.appointmentservice.api;

import com.swasthyasetu.appointmentservice.domain.DoctorAvailability;
import com.swasthyasetu.appointmentservice.dto.AvailabilitySlotRequest;
import com.swasthyasetu.appointmentservice.dto.ReplaceDoctorAvailabilityRequest;
import com.swasthyasetu.appointmentservice.security.CurrentUser;
import com.swasthyasetu.appointmentservice.security.CurrentUserService;
import com.swasthyasetu.appointmentservice.service.DoctorAvailabilityService;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments/doctor/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {
  private final DoctorAvailabilityService doctorAvailabilityService;
  private final CurrentUserService currentUserService;

  @PostMapping
  public ResponseEntity<ApiResponse<List<DoctorAvailability>>> replaceAvailability(
      @RequestBody ReplaceDoctorAvailabilityRequest request
  ) {
    ResponseEntity<ApiResponse<List<DoctorAvailability>>> accessError = validateDoctorAccess();
    if (accessError != null) {
      return accessError;
    }

    if (request == null || request.getSlots() == null) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "slots is required", null)
      ));
    }

    String validationError = validateSlots(request.getSlots());
    if (validationError != null) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", validationError, null)
      ));
    }

    CurrentUser currentUser = currentUserService.getCurrentUser().orElseThrow();
    List<DoctorAvailability> saved = doctorAvailabilityService.replaceAll(
        currentUser.getId(),
        request.getSlots()
    );
    return ResponseEntity.ok(new ApiResponse<>(true, saved, null));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<List<DoctorAvailability>>> getAvailability() {
    ResponseEntity<ApiResponse<List<DoctorAvailability>>> accessError = validateDoctorAccess();
    if (accessError != null) {
      return accessError;
    }

    CurrentUser currentUser = currentUserService.getCurrentUser().orElseThrow();
    List<DoctorAvailability> availability = doctorAvailabilityService.getDoctorAvailability(currentUser.getId());
    return ResponseEntity.ok(new ApiResponse<>(true, availability, null));
  }

  private ResponseEntity<ApiResponse<List<DoctorAvailability>>> validateDoctorAccess() {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"DOCTOR".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctor role required", null)
      ));
    }

    return null;
  }

  private String validateSlots(List<AvailabilitySlotRequest> slots) {
    for (AvailabilitySlotRequest slot : slots) {
      if (slot == null) {
        return "slots cannot contain null entries";
      }
      if (slot.getDayOfWeek() == null || slot.getDayOfWeek() < 0 || slot.getDayOfWeek() > 6) {
        return "dayOfWeek must be between 0 and 6";
      }
      if (slot.getStartTime() == null || slot.getEndTime() == null) {
        return "startTime and endTime are required";
      }
      if (!slot.getStartTime().isBefore(slot.getEndTime())) {
        return "startTime must be before endTime";
      }
      if (slot.getSlotMinutes() == null || slot.getSlotMinutes() <= 0) {
        return "slotMinutes must be greater than 0";
      }
      if (slot.getBufferMinutes() == null || slot.getBufferMinutes() < 0) {
        return "bufferMinutes must be 0 or greater";
      }
    }
    return null;
  }
}
