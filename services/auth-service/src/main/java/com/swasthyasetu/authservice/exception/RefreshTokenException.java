package com.swasthyasetu.authservice.exception;

import lombok.Getter;

@Getter
public class RefreshTokenException extends RuntimeException {
  public enum Status {
    BAD_REQUEST,
    UNAUTHORIZED
  }

  private final String code;
  private final Status status;

  public RefreshTokenException(String code, String message, Status status) {
    super(message);
    this.code = code;
    this.status = status;
  }
}
