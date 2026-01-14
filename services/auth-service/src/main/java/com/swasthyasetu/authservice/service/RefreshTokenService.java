package com.swasthyasetu.authservice.service;

import com.swasthyasetu.authservice.domain.RefreshToken;
import com.swasthyasetu.authservice.exception.RefreshTokenException;
import com.swasthyasetu.authservice.repository.RefreshTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void storeRefreshToken(UUID userId, String refreshToken, LocalDateTime expiresAt) {
    RefreshToken token = new RefreshToken(
        UUID.randomUUID(),
        userId,
        passwordEncoder.encode(refreshToken),
        expiresAt,
        false,
        LocalDateTime.now()
    );
    refreshTokenRepository.save(token);
  }

  @Transactional(readOnly = true)
  public void assertValid(UUID userId, String refreshToken) {
    List<RefreshToken> tokens = refreshTokenRepository
        .findByUserIdAndRevokedFalseAndExpiresAtAfter(userId, LocalDateTime.now());
    boolean match = tokens.stream()
        .anyMatch(token -> passwordEncoder.matches(refreshToken, token.getTokenHash()));
    if (!match) {
      throw new RefreshTokenException(
          "REFRESH_TOKEN_INVALID",
          "Invalid refresh token",
          RefreshTokenException.Status.UNAUTHORIZED
      );
    }
  }
}
