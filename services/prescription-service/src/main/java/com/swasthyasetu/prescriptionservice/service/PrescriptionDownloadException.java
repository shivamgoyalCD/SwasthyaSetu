package com.swasthyasetu.prescriptionservice.service;

public class PrescriptionDownloadException extends RuntimeException {
  public PrescriptionDownloadException(String message) {
    super(message);
  }

  public PrescriptionDownloadException(String message, Throwable cause) {
    super(message, cause);
  }
}
