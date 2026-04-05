package com.swasthyasetu.prescriptionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionDownloadResponse {
  private String downloadUrl;
}
