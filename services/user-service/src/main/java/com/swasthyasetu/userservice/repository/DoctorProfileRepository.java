package com.swasthyasetu.userservice.repository;

import com.swasthyasetu.userservice.domain.DoctorProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {
  Optional<DoctorProfile> findByUserId(UUID userId);

  List<DoctorProfile> findByStatus(String status);
}
