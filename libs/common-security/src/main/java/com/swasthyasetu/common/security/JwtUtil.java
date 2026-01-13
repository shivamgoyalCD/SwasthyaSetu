package com.swasthyasetu.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class JwtUtil {
  private final JwtParser parser;
  private final Key key;
  private final Duration tokenTtl;

  public JwtUtil(String secret, Duration tokenTtl) {
    if (secret == null || secret.isBlank()) {
      throw new IllegalArgumentException("secret must be set");
    }
    if (tokenTtl == null || tokenTtl.isZero() || tokenTtl.isNegative()) {
      throw new IllegalArgumentException("tokenTtl must be positive");
    }
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.tokenTtl = tokenTtl;
    this.parser = Jwts.parserBuilder().setSigningKey(key).build();
  }

  public String createToken(String userId, Role role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .setSubject(userId)
        .claim("role", role.name())
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(now.plus(tokenTtl)))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      parser.parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException ex) {
      return false;
    }
  }

  public Claims extractClaims(String token) {
    return parser.parseClaimsJws(token).getBody();
  }
}
