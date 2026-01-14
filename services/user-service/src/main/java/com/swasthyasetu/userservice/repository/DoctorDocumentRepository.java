package com.swasthyasetu.userservice.repository;

import com.swasthyasetu.userservice.domain.DoctorDocument;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorDocumentRepository extends JpaRepository<DoctorDocument, UUID> {
}
