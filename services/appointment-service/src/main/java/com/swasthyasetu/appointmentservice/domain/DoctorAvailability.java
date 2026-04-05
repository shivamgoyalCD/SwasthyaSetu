package com.swasthyasetu.appointmentservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "doctor_availability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailability {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "doctor_id", nullable = false, columnDefinition = "uuid")
  private UUID doctorId;

  @Column(name = "day_of_week", nullable = false)
  private int dayOfWeek;

  @Column(name = "start_time", nullable = false)
  private LocalTime startTime;

  @Column(name = "end_time", nullable = false)
  private LocalTime endTime;

  @Column(name = "slot_minutes", nullable = false)
  private int slotMinutes;

  @Column(name = "buffer_minutes", nullable = false)
  private int bufferMinutes;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
