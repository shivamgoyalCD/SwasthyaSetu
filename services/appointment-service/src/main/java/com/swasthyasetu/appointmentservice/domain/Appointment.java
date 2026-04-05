package com.swasthyasetu.appointmentservice.domain;

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
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "patient_id", nullable = false, columnDefinition = "uuid")
  private UUID patientId;

  @Column(name = "doctor_id", nullable = false, columnDefinition = "uuid")
  private UUID doctorId;

  @Column(name = "start_ts", nullable = false)
  private LocalDateTime startTs;

  @Column(name = "end_ts", nullable = false)
  private LocalDateTime endTs;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
