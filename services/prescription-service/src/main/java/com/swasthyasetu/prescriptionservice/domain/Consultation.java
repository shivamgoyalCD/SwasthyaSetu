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
@Table(name = "consultations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "appointment_id", nullable = false, unique = true, columnDefinition = "uuid")
  private UUID appointmentId;

  @Column(name = "status")
  private String status;

  @Column(name = "started_at")
  private LocalDateTime startedAt;

  @Column(name = "ended_at")
  private LocalDateTime endedAt;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
