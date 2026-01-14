package com.swasthyasetu.authservice.dto;

public class RequestOtpRequest {
  private String phone;

  public RequestOtpRequest() {
  }

  public RequestOtpRequest(String phone) {
    this.phone = phone;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }
}
