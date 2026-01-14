package com.swasthyasetu.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDocsPresignRequest {
  private String docType;
  private String fileName;
  private String contentType;
}
