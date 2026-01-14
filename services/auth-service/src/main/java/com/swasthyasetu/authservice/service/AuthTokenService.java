package com.swasthyasetu.authservice.service;

import com.swasthyasetu.authservice.exception.RefreshTokenException;
import com.swasthyasetu.common.security.JwtUtil;
import com.swasthyasetu.common.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenService {
  private final JwtUtil accessJwtUtil;
  private final JwtUtil refreshJwtUtil;
  private final RefreshTokenService refreshTokenService;

  public AuthTokenService(
      @Qualifier("accessJwtUtil") JwtUtil accessJwtUtil,
      @Qualifier("refreshJwtUtil") JwtUtil refreshJwtUtil,
      RefreshTokenService refreshTokenService
  ) {
    this.accessJwtUtil = accessJwtUtil;
    this.refreshJwtUtil = refreshJwtUtil;
    this.refreshTokenService = refreshTokenService;
  }

  public TokenPair issueTokens(String phone, Role role) {
    String userId = UUID.nameUUIDFromBytes(phone.getBytes(StandardCharsets.UTF_8)).toString();
    Role resolvedRole = role == null ? Role.PATIENT : role;
    String accessToken = accessJwtUtil.createToken(userId, resolvedRole);
    String refreshToken = refreshJwtUtil.createToken(userId, resolvedRole);
    Date refreshExpiry = refreshJwtUtil.extractClaims(refreshToken).getExpiration();
    refreshTokenService.storeRefreshToken(
        UUID.fromString(userId),
        refreshToken,
        toLocalDateTime(refreshExpiry)
    );
    return new TokenPair(accessToken, refreshToken);
  }

  public String refreshAccessToken(String refreshToken) {
    if (!refreshJwtUtil.validateToken(refreshToken)) {
      throw new RefreshTokenException(
          "REFRESH_TOKEN_INVALID",
          "Invalid refresh token",
          RefreshTokenException.Status.UNAUTHORIZED
      );
    }

    Claims claims = extractClaims(refreshToken);
    String userId = claims.getSubject();
    Role role = parseRole(claims.get(JwtUtil.ROLE_CLAIM, String.class));

    refreshTokenService.assertValid(UUID.fromString(userId), refreshToken);
    return accessJwtUtil.createToken(userId, role);
  }

  private Claims extractClaims(String refreshToken) {
    try {
      return refreshJwtUtil.extractClaims(refreshToken);
    } catch (JwtException | IllegalArgumentException ex) {
      throw new RefreshTokenException(
          "REFRESH_TOKEN_INVALID",
          "Invalid refresh token",
          RefreshTokenException.Status.UNAUTHORIZED
      );
    }
  }

  private Role parseRole(String role) {
    if (role == null || role.isBlank()) {
      return Role.PATIENT;
    }
    try {
      return Role.valueOf(role);
    } catch (IllegalArgumentException ex) {
      return Role.PATIENT;
    }
  }

  private LocalDateTime toLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
}
