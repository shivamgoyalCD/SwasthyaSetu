package com.swasthyasetu.userservice.api;

import com.swasthyasetu.common.dtos.ApiError;
import com.swasthyasetu.common.dtos.ApiResponse;
import com.swasthyasetu.userservice.domain.DoctorProfile;
import com.swasthyasetu.userservice.domain.User;
import com.swasthyasetu.userservice.dto.DoctorDocsPresignRequest;
import com.swasthyasetu.userservice.dto.DoctorDocsPresignResponse;
import com.swasthyasetu.userservice.dto.DoctorOnboardRequest;
import com.swasthyasetu.userservice.dto.UpdateUserRequest;
import com.swasthyasetu.userservice.security.CurrentUser;
import com.swasthyasetu.userservice.security.CurrentUserService;
import com.swasthyasetu.userservice.service.AdminDoctorService;
import com.swasthyasetu.userservice.service.DoctorService;
import com.swasthyasetu.userservice.service.UserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final DoctorService doctorService;
  private final AdminDoctorService adminDoctorService;
  private final CurrentUserService currentUserService;

  @GetMapping("/users/me")
  public ApiResponse<User> me() {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return new ApiResponse<>(false, null, null);
    }

    CurrentUser user = currentUser.get();
    User result = userService.getOrCreate(user.getId(), user.getRole());
    return new ApiResponse<>(true, result, null);
  }

  @PatchMapping("/users/me")
  public ApiResponse<User> updateMe(@RequestBody UpdateUserRequest request) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return new ApiResponse<>(false, null, null);
    }

    CurrentUser user = currentUser.get();
    User updated = userService.update(user.getId(), user.getRole(), request);
    return new ApiResponse<>(true, updated, null);
  }

  @PostMapping("/users/doctor/onboard")
  public ResponseEntity<ApiResponse<DoctorProfile>> onboardDoctor(
      @RequestBody DoctorOnboardRequest request
  ) {
    if (request == null) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "Request body is required", null)
      ));
    }

    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    CurrentUser user = currentUser.get();
    if (!"DOCTOR".equals(user.getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctor role required", null)
      ));
    }

    DoctorProfile profile = doctorService.onboard(user.getId(), user.getRole(), request);
    return ResponseEntity.ok(new ApiResponse<>(true, profile, null));
  }

  @PostMapping("/users/doctor/docs/presign")
  public ResponseEntity<ApiResponse<DoctorDocsPresignResponse>> presignDoctorDoc(
      @RequestBody DoctorDocsPresignRequest request
  ) {
    if (request == null
        || isBlank(request.getDocType())
        || isBlank(request.getFileName())) {
      return ResponseEntity.badRequest().body(new ApiResponse<>(
          false,
          null,
          new ApiError("INVALID_REQUEST", "docType and fileName are required", null)
      ));
    }

    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    CurrentUser user = currentUser.get();
    if (!"DOCTOR".equals(user.getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Doctor role required", null)
      ));
    }

    try {
      Optional<DoctorDocsPresignResponse> response = doctorService.presignDoc(user.getId(), request);
      if (response.isEmpty()) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(
            false,
            null,
            new ApiError("DOCTOR_PROFILE_NOT_FOUND", "Doctor profile not found", null)
        ));
      }
      return ResponseEntity.ok(new ApiResponse<>(true, response.get(), null));
    } catch (Exception ex) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
          false,
          null,
          new ApiError("PRESIGN_FAILED", "Failed to generate upload URL", null)
      ));
    }
  }

  @PostMapping("/users/admin/doctor/{doctorId}/verify")
  public ResponseEntity<ApiResponse<DoctorProfile>> verifyDoctor(
      @PathVariable("doctorId") UUID doctorId
  ) {
    return updateDoctorStatus(doctorId, "VERIFIED");
  }

  @PostMapping("/users/admin/doctor/{doctorId}/reject")
  public ResponseEntity<ApiResponse<DoctorProfile>> rejectDoctor(
      @PathVariable("doctorId") UUID doctorId
  ) {
    return updateDoctorStatus(doctorId, "REJECTED");
  }

  @GetMapping("/users/admin/doctors/pending")
  public ResponseEntity<ApiResponse<List<DoctorProfile>>> pendingDoctors() {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    CurrentUser user = currentUser.get();
    if (!"ADMIN".equals(user.getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Admin role required", null)
      ));
    }

    List<DoctorProfile> pending = adminDoctorService.listPending();
    return ResponseEntity.ok(new ApiResponse<>(true, pending, null));
  }

  private ResponseEntity<ApiResponse<DoctorProfile>> updateDoctorStatus(
      UUID doctorId,
      String status
  ) {
    Optional<CurrentUser> currentUser = currentUserService.getCurrentUser();
    if (currentUser.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(
          false,
          null,
          new ApiError("UNAUTHORIZED", "Authentication required", null)
      ));
    }

    CurrentUser user = currentUser.get();
    if (!"ADMIN".equals(user.getRole())) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse<>(
          false,
          null,
          new ApiError("FORBIDDEN", "Admin role required", null)
      ));
    }

    return adminDoctorService.updateStatus(doctorId, status)
        .map(profile -> ResponseEntity.ok(new ApiResponse<>(true, profile, null)))
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(
            false,
            null,
            new ApiError("DOCTOR_PROFILE_NOT_FOUND", "Doctor profile not found", null)
        )));
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }
}
