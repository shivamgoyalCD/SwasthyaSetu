package com.swasthyasetu.appointmentservice.service;

public class AppointmentCancellationForbiddenException extends RuntimeException {
  public AppointmentCancellationForbiddenException(String message) {
    super(message);
  }
}
