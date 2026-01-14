package com.swasthyasetu.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDocsPresignResponse {
  private String uploadUrl;
  private String s3Key;
}
