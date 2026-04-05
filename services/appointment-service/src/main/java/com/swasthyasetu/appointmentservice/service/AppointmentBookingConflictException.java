package com.swasthyasetu.appointmentservice.service;

public class AppointmentBookingConflictException extends RuntimeException {
  public AppointmentBookingConflictException(String message, Throwable cause) {
    super(message, cause);
  }
}
