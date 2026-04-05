package com.swasthyasetu.prescriptionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.swasthyasetu.prescriptionservice.domain.LlmSummary;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LlmSummaryResponse {
  private UUID id;

  @JsonProperty("consultation_id")
  private UUID consultationId;

  @JsonProperty("json_summary")
  private String jsonSummary;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  public static LlmSummaryResponse fromEntity(LlmSummary summary) {
    return new LlmSummaryResponse(
        summary.getId(),
        summary.getConsultationId(),
        summary.getJsonSummary(),
        summary.getCreatedAt()
    );
  }
}
