package com.swasthyasetu.prescriptionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePrescriptionResponse {
  @JsonProperty("prescriptionId")
  private UUID prescriptionId;
}
