package com.swasthyasetu.authservice.repository;

import com.swasthyasetu.authservice.domain.OtpSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpSessionRepository extends JpaRepository<OtpSession, UUID> {
}
