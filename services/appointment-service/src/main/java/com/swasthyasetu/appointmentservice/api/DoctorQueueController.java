package com.swasthyasetu.appointmentservice.api;

import com.swasthyasetu.appointmentservice.dto.DoctorQueueItemResponse;
import com.swasthyasetu.appointmentservice.security.CurrentUser;
import com.swasthyasetu.appointmentservice.security.CurrentUserService;
import com.swasthyasetu.appointmentservice.service.AppointmentWaitingRoomService;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DoctorQueueController {
  private final AppointmentWaitingRoomService appointmentWaitingRoomService;
  private final CurrentUserService currentUserService;

  @GetMapping("/appointments/doctor/queue")
  public ResponseEntity<ApiResponse<List<DoctorQueueItemResponse>>> getQueue(
      @RequestParam("doctorId") String doctorId
  ) {
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

    UUID parsedDoctorId;
    try {
      parsedDoctorId = UUID.fromString(doctorId);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_DOCTOR_ID", "doctorId must be a valid UUID", null)
      ));
    }

    if (!parsedDoctorId.equals(currentUser.get().getId())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctors can only access their own queue", null)
      ));
    }

    List<DoctorQueueItemResponse> queue = appointmentWaitingRoomService.getDoctorQueue(
        parsedDoctorId,
        LocalDate.now()
    );
    return ResponseEntity.ok(new ApiResponse<>(true, queue, null));
  }
}
