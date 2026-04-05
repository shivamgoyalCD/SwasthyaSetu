package com.swasthyasetu.appointmentservice.dto;

import lombok.Data;

@Data
public class BookAppointmentRequest {
  private String doctorId;
  private String startTs;
}
