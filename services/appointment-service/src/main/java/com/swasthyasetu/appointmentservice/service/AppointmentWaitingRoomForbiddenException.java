package com.swasthyasetu.appointmentservice.service;

public class AppointmentWaitingRoomForbiddenException extends RuntimeException {
  public AppointmentWaitingRoomForbiddenException(String message) {
    super(message);
  }
}
