package com.swasthyasetu.prescriptionservice.repository;

import com.swasthyasetu.prescriptionservice.domain.Prescription;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
  Optional<Prescription> findByConsultationId(UUID consultationId);
}
