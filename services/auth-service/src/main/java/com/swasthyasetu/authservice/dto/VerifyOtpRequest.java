package com.swasthyasetu.authservice.dto;

public class VerifyOtpRequest {
  private String sessionId;
  private String otp;

  public VerifyOtpRequest() {
  }

  public VerifyOtpRequest(String sessionId, String otp) {
    this.sessionId = sessionId;
    this.otp = otp;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getOtp() {
    return otp;
  }

  public void setOtp(String otp) {
    this.otp = otp;
  }
}
