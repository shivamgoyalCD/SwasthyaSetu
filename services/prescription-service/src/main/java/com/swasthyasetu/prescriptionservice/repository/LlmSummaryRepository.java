package com.swasthyasetu.prescriptionservice.repository;

import com.swasthyasetu.prescriptionservice.domain.LlmSummary;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LlmSummaryRepository extends JpaRepository<LlmSummary, UUID> {
  Optional<LlmSummary> findByConsultationId(UUID consultationId);
}
