package com.swasthyasetu.authservice.exception;

public class OtpVerificationException extends RuntimeException {
  public enum Status {
    BAD_REQUEST,
    FORBIDDEN
  }

  private final String code;
  private final Status status;

  public OtpVerificationException(String code, String message, Status status) {
    super(message);
    this.code = code;
    this.status = status;
  }

  public String getCode() {
    return code;
  }

  public Status getStatus() {
    return status;
  }
}
