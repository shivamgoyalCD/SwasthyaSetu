package com.swasthyasetu.prescriptionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateConsultationSummaryRequest {
  @JsonProperty("json_summary")
  private String jsonSummary;
}
