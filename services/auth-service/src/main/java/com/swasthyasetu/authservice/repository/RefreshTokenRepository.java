package com.swasthyasetu.authservice.repository;

import com.swasthyasetu.authservice.domain.RefreshToken;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  List<RefreshToken> findByUserIdAndRevokedFalseAndExpiresAtAfter(
      UUID userId,
      LocalDateTime now
  );
}
