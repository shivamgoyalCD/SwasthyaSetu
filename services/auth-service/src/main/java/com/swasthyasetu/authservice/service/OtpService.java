package com.swasthyasetu.authservice.service;

import com.swasthyasetu.authservice.domain.OtpSession;
import com.swasthyasetu.authservice.exception.RequestOtpException;
import com.swasthyasetu.authservice.exception.OtpVerificationException;
import com.swasthyasetu.authservice.repository.OtpSessionRepository;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {
  private static final SecureRandom random = new SecureRandom();
  private static final int MAX_OTP_REQUESTS = 3;
  private static final Duration OTP_RATE_LIMIT_WINDOW = Duration.ofMinutes(10);

  private final OtpSessionRepository otpSessionRepository;
  private final PasswordEncoder passwordEncoder;
  private final StringRedisTemplate stringRedisTemplate;

  public UUID createOtpSession(String phone) {
    enforceOtpRateLimit(phone);

    String otp = generateOtp();
    String otpHash = passwordEncoder.encode(otp);
    UUID sessionId = UUID.randomUUID();

    OtpSession session = new OtpSession(
        sessionId,
        phone,
        otpHash,
        LocalDateTime.now().plusMinutes(5),
        0,
        "CREATED",
        LocalDateTime.now()
    );
    otpSessionRepository.save(session);

    log.info("OTP for {} is {}", phone, otp);
    return sessionId;
  }

  private void enforceOtpRateLimit(String phone) {
    if (phone == null || phone.isBlank()) {
      throw new RequestOtpException(
          "INVALID_PHONE",
          "phone is required",
          RequestOtpException.Status.BAD_REQUEST
      );
    }

    String rateLimitKey = "otp:rate:" + phone.trim();
    Long requestCount = stringRedisTemplate.opsForValue().increment(rateLimitKey);

    if (requestCount == null) {
      throw new RequestOtpException(
          "OTP_RATE_LIMIT_UNAVAILABLE",
          "Failed to evaluate OTP rate limit",
          RequestOtpException.Status.BAD_REQUEST
      );
    }

    if (requestCount == 1L) {
      stringRedisTemplate.expire(rateLimitKey, OTP_RATE_LIMIT_WINDOW);
    }

    if (requestCount > MAX_OTP_REQUESTS) {
      throw new RequestOtpException(
          "OTP_RATE_LIMIT_EXCEEDED",
          "OTP request limit exceeded. Try again later.",
          RequestOtpException.Status.TOO_MANY_REQUESTS
      );
    }
  }

  @Transactional
  public OtpSession verifyOtp(UUID sessionId, String otp) {
    OtpSession session = otpSessionRepository.findById(sessionId)
        .orElseThrow(() -> new OtpVerificationException(
            "OTP_SESSION_NOT_FOUND",
            "OTP session not found",
            OtpVerificationException.Status.BAD_REQUEST
        ));

    if (session.getAttempts() >= 5) {
      if (!"LOCKED".equals(session.getStatus())) {
        session.setStatus("LOCKED");
        otpSessionRepository.save(session);
      }
      throw new OtpVerificationException(
          "OTP_ATTEMPTS_EXCEEDED",
          "OTP attempts exceeded",
          OtpVerificationException.Status.FORBIDDEN
      );
    }

    if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
      session.setStatus("EXPIRED");
      otpSessionRepository.save(session);
      throw new OtpVerificationException(
          "OTP_EXPIRED",
          "OTP expired",
          OtpVerificationException.Status.FORBIDDEN
      );
    }

    if (!"CREATED".equals(session.getStatus())) {
      throw new OtpVerificationException(
          "OTP_INVALID",
          "OTP session is not active",
          OtpVerificationException.Status.BAD_REQUEST
      );
    }

    if (otp == null || !passwordEncoder.matches(otp, session.getOtpHash())) {
      int nextAttempts = session.getAttempts() + 1;
      session.setAttempts(nextAttempts);
      if (nextAttempts >= 5) {
        session.setStatus("LOCKED");
      }
      otpSessionRepository.save(session);
      throw new OtpVerificationException(
          "OTP_INVALID",
          "Invalid OTP",
          OtpVerificationException.Status.BAD_REQUEST
      );
    }

    session.setStatus("USED");
    otpSessionRepository.save(session);
    return session;
  }

  private String generateOtp() {
    int value = 100000 + random.nextInt(900000);
    return String.valueOf(value);
  }
}
