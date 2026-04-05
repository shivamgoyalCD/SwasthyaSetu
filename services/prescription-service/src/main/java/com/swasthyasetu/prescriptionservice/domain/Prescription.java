package com.swasthyasetu.prescriptionservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "consultation_id", nullable = false, unique = true, columnDefinition = "uuid")
  private UUID consultationId;

  @Column(name = "pdf_s3_key")
  private String pdfS3Key;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
