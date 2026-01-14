package com.swasthyasetu.authservice.service;

import com.swasthyasetu.authservice.domain.OtpSession;
import com.swasthyasetu.authservice.exception.OtpVerificationException;
import com.swasthyasetu.authservice.repository.OtpSessionRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {
  private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
  private static final SecureRandom random = new SecureRandom();

  private final OtpSessionRepository otpSessionRepository;
  private final PasswordEncoder passwordEncoder;

  public OtpService(OtpSessionRepository otpSessionRepository, PasswordEncoder passwordEncoder) {
    this.otpSessionRepository = otpSessionRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UUID createOtpSession(String phone) {
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

    logger.info("OTP for {} is {}", phone, otp);
    return sessionId;
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
