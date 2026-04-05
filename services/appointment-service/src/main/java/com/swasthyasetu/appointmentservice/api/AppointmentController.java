package com.swasthyasetu.appointmentservice.api;

import com.swasthyasetu.appointmentservice.domain.Appointment;
import com.swasthyasetu.appointmentservice.dto.BookAppointmentRequest;
import com.swasthyasetu.appointmentservice.service.AppointmentCancellationForbiddenException;
import com.swasthyasetu.appointmentservice.service.AppointmentWaitingRoomForbiddenException;
import com.swasthyasetu.appointmentservice.service.AppointmentWaitingRoomInvalidStateException;
import com.swasthyasetu.appointmentservice.security.CurrentUser;
import com.swasthyasetu.appointmentservice.security.CurrentUserService;
import com.swasthyasetu.appointmentservice.service.AppointmentBookingConflictException;
import com.swasthyasetu.appointmentservice.service.AppointmentBookingService;
import com.swasthyasetu.appointmentservice.service.AppointmentWaitingRoomService;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AppointmentController {
  private final AppointmentBookingService appointmentBookingService;
  private final AppointmentWaitingRoomService appointmentWaitingRoomService;
  private final CurrentUserService currentUserService;

  @PostMapping("/appointments/book")
  public ResponseEntity<ApiResponse<Appointment>> book(@RequestBody BookAppointmentRequest request) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"PATIENT".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Patient role required", null)
      ));
    }

    if (request == null || isBlank(request.getDoctorId()) || isBlank(request.getStartTs())) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "doctorId and startTs are required", null)
      ));
    }

    UUID doctorId;
    try {
      doctorId = UUID.fromString(request.getDoctorId());
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_DOCTOR_ID", "doctorId must be a valid UUID", null)
      ));
    }

    LocalDateTime startTs;
    try {
      startTs = LocalDateTime.parse(request.getStartTs());
    } catch (DateTimeParseException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_START_TS", "startTs must be a valid ISO local datetime", null)
      ));
    }

    try {
      return appointmentBookingService.book(currentUser.get().getId(), doctorId, startTs)
          .map(appointment -> ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
              true,
              appointment,
              null
          )))
          .orElseGet(() -> ResponseEntity.badRequest().body(new ApiResponse<>(
              false,
              null,
              new ApiError("INVALID_SLOT", "Requested slot is not available in doctor availability", null)
          )));
    } catch (AppointmentBookingConflictException ex) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(
          false,
          null,
          new ApiError("SLOT_ALREADY_BOOKED", "Requested slot is already booked", null)
      ));
    }
  }

  @PostMapping("/appointments/{id}/cancel")
  public ResponseEntity<ApiResponse<Appointment>> cancel(@PathVariable("id") String id) {
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

    UUID appointmentId;
    try {
      appointmentId = UUID.fromString(id);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_APPOINTMENT_ID", "id must be a valid UUID", null)
      ));
    }

    try {
      return appointmentBookingService.cancel(
              appointmentId,
              currentUser.get().getId(),
              currentUser.get().getRole()
          )
          .map(appointment -> ResponseEntity.ok(new ApiResponse<>(true, appointment, null)))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
              false,
              null,
              new ApiError("APPOINTMENT_NOT_FOUND", "Appointment not found", null)
          )));
    } catch (AppointmentCancellationForbiddenException ex) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Only the booked patient or assigned doctor can cancel", null)
      ));
    }
  }

  @PostMapping("/appointments/{id}/join-waiting-room")
  public ResponseEntity<ApiResponse<Appointment>> joinWaitingRoom(@PathVariable("id") String id) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    if (!"PATIENT".equals(currentUser.get().getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Patient role required", null)
      ));
    }

    UUID appointmentId;
    try {
      appointmentId = UUID.fromString(id);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_APPOINTMENT_ID", "id must be a valid UUID", null)
      ));
    }

    try {
      return appointmentWaitingRoomService.joinWaitingRoom(appointmentId, currentUser.get().getId())
          .map(appointment -> ResponseEntity.ok(new ApiResponse<>(true, appointment, null)))
          .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
              false,
              null,
              new ApiError("APPOINTMENT_NOT_FOUND", "Appointment not found", null)
          )));
    } catch (AppointmentWaitingRoomForbiddenException ex) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Only the booked patient can join the waiting room", null)
      ));
    } catch (AppointmentWaitingRoomInvalidStateException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_APPOINTMENT_STATE", "Only booked appointments can join the waiting room", null)
      ));
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
