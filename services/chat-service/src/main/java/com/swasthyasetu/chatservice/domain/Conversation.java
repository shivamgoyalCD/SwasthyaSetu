package com.swasthyasetu.chatservice.domain;

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
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "appointment_id", nullable = false, unique = true, columnDefinition = "uuid")
  private UUID appointmentId;

  @Column(name = "patient_id", columnDefinition = "uuid")
  private UUID patientId;

  @Column(name = "doctor_id", columnDefinition = "uuid")
  private UUID doctorId;

  @Column(name = "status")
  private String status;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
