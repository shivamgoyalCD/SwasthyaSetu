package com.swasthyasetu.common.dtos;

public class ApiError {
  private String code;
  private String message;
  private String traceId;

  public ApiError() {
  }

  public ApiError(String code, String message, String traceId) {
    this.code = code;
    this.message = message;
    this.traceId = traceId;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String traceId) {
    this.traceId = traceId;
  }
}
