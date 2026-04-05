package com.swasthyasetu.appointmentservice.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DoctorQueueItemResponse {
  private UUID appointmentId;
  private boolean presence;
}
