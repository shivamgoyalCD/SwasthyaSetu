package com.swasthyasetu.authservice.exception;

import lombok.Getter;

@Getter
public class RequestOtpException extends RuntimeException {
  public enum Status {
    BAD_REQUEST,
    TOO_MANY_REQUESTS
  }

  private final String code;
  private final Status status;

  public RequestOtpException(String code, String message, Status status) {
    super(message);
    this.code = code;
    this.status = status;
  }
}
