package com.swasthyasetu.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorOnboardRequest {
  private String specialization;
  private String licenseNo;
  private Integer experienceYears;
}
