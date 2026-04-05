package com.swasthyasetu.notificationservice.domain;

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
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "type")
  private String type;

  @Column(name = "payload", columnDefinition = "text")
  private String payload;

  @Column(name = "status")
  private String status;

  @Column(name = "retries")
  private Integer retries;

  @Column(name = "created_at")
  private LocalDateTime createdAt;
}
