package com.swasthyasetu.appointmentservice.dto;

import java.time.LocalTime;
import lombok.Data;

@Data
public class AvailabilitySlotRequest {
  private Integer dayOfWeek;
  private LocalTime startTime;
  private LocalTime endTime;
  private Integer slotMinutes;
  private Integer bufferMinutes;
}
