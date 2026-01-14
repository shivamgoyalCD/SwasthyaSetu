package com.swasthyasetu.authservice.api;

import com.swasthyasetu.authservice.dto.RequestOtpRequest;
import com.swasthyasetu.authservice.dto.RequestOtpResponse;
import com.swasthyasetu.authservice.dto.RefreshTokenRequest;
import com.swasthyasetu.authservice.dto.RefreshTokenResponse;
import com.swasthyasetu.authservice.dto.VerifyOtpRequest;
import com.swasthyasetu.authservice.dto.VerifyOtpResponse;
import com.swasthyasetu.authservice.service.OtpService;
import com.swasthyasetu.authservice.exception.OtpVerificationException;
import com.swasthyasetu.authservice.exception.RefreshTokenException;
import com.swasthyasetu.authservice.service.AuthTokenService;
import com.swasthyasetu.authservice.service.TokenPair;
import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import com.swasthyasetu.common.security.Role;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final OtpService otpService;
  private final AuthTokenService authTokenService;

  public AuthController(OtpService otpService, AuthTokenService authTokenService) {
    this.otpService = otpService;
    this.authTokenService = authTokenService;
  }

  @PostMapping("/request-otp")
  public ApiResponse<RequestOtpResponse> requestOtp(@RequestBody RequestOtpRequest request) {
    UUID sessionId = otpService.createOtpSession(request.getPhone());
    return new ApiResponse<>(true, new RequestOtpResponse(sessionId.toString()), null);
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(
      @RequestBody VerifyOtpRequest request
  ) {
    if (request == null
        || request.getSessionId() == null
        || request.getSessionId().isBlank()
        || request.getOtp() == null
        || request.getOtp().isBlank()) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "sessionId and otp are required", null)
      ));
    }

    try {
      UUID sessionId = UUID.fromString(request.getSessionId());
      String phone = otpService.verifyOtp(sessionId, request.getOtp()).getPhone();
      TokenPair tokenPair = authTokenService.issueTokens(phone, Role.PATIENT);
      VerifyOtpResponse response = new VerifyOtpResponse(
          tokenPair.getAccessToken(),
          tokenPair.getRefreshToken()
      );
      return ResponseEntity.ok(new ApiResponse<>(true, response, null));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_SESSION_ID", "Invalid sessionId", null)
      ));
    } catch (OtpVerificationException ex) {
      HttpStatus status = ex.getStatus() == OtpVerificationException.Status.FORBIDDEN
          ? HttpStatus.FORBIDDEN
          : HttpStatus.BAD_REQUEST;
      return ResponseEntity.status(status).body(new ApiResponse<>(
          false,
          null,
          new ApiError(ex.getCode(), ex.getMessage(), null)
      ));
    }
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshTokenResponse>> refresh(
      @RequestBody RefreshTokenRequest request
  ) {
    if (request == null
        || request.getRefreshToken() == null
        || request.getRefreshToken().isBlank()) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "refreshToken is required", null)
      ));
    }

    try {
      String accessToken = authTokenService.refreshAccessToken(request.getRefreshToken());
      return ResponseEntity.ok(new ApiResponse<>(
          true,
          new RefreshTokenResponse(accessToken),
          null
      ));
    } catch (RefreshTokenException ex) {
      HttpStatus status = ex.getStatus() == RefreshTokenException.Status.UNAUTHORIZED
          ? HttpStatus.UNAUTHORIZED
          : HttpStatus.BAD_REQUEST;
      return ResponseEntity.status(status).body(new ApiResponse<>(
          false,
          null,
          new ApiError(ex.getCode(), ex.getMessage(), null)
      ));
    }
  }
}
