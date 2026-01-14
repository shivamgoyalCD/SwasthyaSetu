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
@Table(name = "doctor_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfile {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "specialization")
  private String specialization;

  @Column(name = "license_no")
  private String licenseNo;

  @Column(name = "status")
  private String status;

  @Column(name = "experience_years")
  private Integer experienceYears;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
