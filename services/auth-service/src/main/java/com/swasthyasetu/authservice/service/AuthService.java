package com.swasthyasetu.authservice.service;

import com.swasthyasetu.authservice.dto.RefreshTokenResponse;
import com.swasthyasetu.authservice.dto.VerifyOtpResponse;
import com.swasthyasetu.common.security.Role;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final OtpService otpService;
  private final AuthTokenService authTokenService;

  public UUID requestOtp(String phone) {
    return otpService.createOtpSession(phone);
  }

  public VerifyOtpResponse verifyOtp(UUID sessionId, String otp) {
    String phone = otpService.verifyOtp(sessionId, otp).getPhone();
    TokenPair tokenPair = authTokenService.issueTokens(phone, Role.PATIENT);
    return new VerifyOtpResponse(tokenPair.getAccessToken(), tokenPair.getRefreshToken());
  }

  public RefreshTokenResponse refresh(String refreshToken) {
    String accessToken = authTokenService.refreshAccessToken(refreshToken);
    return new RefreshTokenResponse(accessToken);
  }
}
