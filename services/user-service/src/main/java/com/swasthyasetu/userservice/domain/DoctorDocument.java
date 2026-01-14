package com.swasthyasetu.userservice.domain;

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
@Table(name = "doctor_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDocument {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "doctor_id", nullable = false, columnDefinition = "uuid")
  private UUID doctorId;

  @Column(name = "doc_type")
  private String docType;

  @Column(name = "s3_key")
  private String s3Key;

  @Column(name = "status")
  private String status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
