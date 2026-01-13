package com.swasthyasetu.common.dtos;

public class ApiResponse<T> {
  private boolean success;
  private T data;
  private ApiError error;

  public ApiResponse() {
  }

  public ApiResponse(boolean success, T data, ApiError error) {
    this.success = success;
    this.data = data;
    this.error = error;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public ApiError getError() {
    return error;
  }

  public void setError(ApiError error) {
    this.error = error;
  }
}
