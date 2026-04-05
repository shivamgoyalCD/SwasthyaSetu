package com.swasthyasetu.prescriptionservice.repository;

import com.swasthyasetu.prescriptionservice.domain.Consultation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
  Optional<Consultation> findByAppointmentId(UUID appointmentId);
}
