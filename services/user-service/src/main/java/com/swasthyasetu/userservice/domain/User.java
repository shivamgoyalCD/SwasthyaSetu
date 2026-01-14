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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @Column(name = "id", nullable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "phone", unique = true)
  private String phone;

  @Column(name = "role", nullable = false)
  private String role;

  @Column(name = "name")
  private String name;

  @Column(name = "language")
  private String language;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}
