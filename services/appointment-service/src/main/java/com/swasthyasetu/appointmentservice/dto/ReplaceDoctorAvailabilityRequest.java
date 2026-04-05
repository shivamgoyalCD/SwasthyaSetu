package com.swasthyasetu.appointmentservice.dto;

import java.util.List;
import lombok.Data;

@Data
public class ReplaceDoctorAvailabilityRequest {
  private List<AvailabilitySlotRequest> slots;
}
