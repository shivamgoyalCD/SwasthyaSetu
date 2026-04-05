package com.swasthyasetu.appointmentservice.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentSlotResponse {
  private LocalDateTime startTs;
  private LocalDateTime endTs;
}
