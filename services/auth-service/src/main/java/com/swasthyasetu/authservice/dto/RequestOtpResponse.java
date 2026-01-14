package com.swasthyasetu.authservice.dto;

public class RequestOtpResponse {
  private String sessionId;

  public RequestOtpResponse() {
  }

  public RequestOtpResponse(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }
}
