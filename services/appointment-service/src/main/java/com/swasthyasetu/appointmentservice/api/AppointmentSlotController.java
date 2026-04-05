package com.swasthyasetu.appointmentservice.api;

import com.swasthyasetu.appointmentservice.dto.AppointmentSlotResponse;
import com.swasthyasetu.appointmentservice.service.AppointmentSlotService;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AppointmentSlotController {
  private final AppointmentSlotService appointmentSlotService;

  @GetMapping("/appointments/slots")
  public ResponseEntity<ApiResponse<List<AppointmentSlotResponse>>> getSlots(
      @RequestParam("doctorId") String doctorId,
      @RequestParam("date") String date
  ) {
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

    LocalDate parsedDate;
    try {
      parsedDate = LocalDate.parse(date);
    } catch (DateTimeParseException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_DATE", "date must be in YYYY-MM-DD format", null)
      ));
    }

    List<AppointmentSlotResponse> slots = appointmentSlotService.getAvailableSlots(parsedDoctorId, parsedDate);
    return ResponseEntity.ok(new ApiResponse<>(true, slots, null));
  }
}
