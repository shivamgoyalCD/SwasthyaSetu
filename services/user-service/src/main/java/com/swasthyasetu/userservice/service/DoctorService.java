package com.swasthyasetu.userservice.service;

import com.swasthyasetu.userservice.domain.DoctorDocument;
import com.swasthyasetu.userservice.domain.DoctorProfile;
import com.swasthyasetu.userservice.dto.DoctorDocsPresignRequest;
import com.swasthyasetu.userservice.dto.DoctorDocsPresignResponse;
import com.swasthyasetu.userservice.dto.DoctorOnboardRequest;
import com.swasthyasetu.userservice.repository.DoctorDocumentRepository;
import com.swasthyasetu.userservice.repository.DoctorProfileRepository;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DoctorService {
  private final DoctorProfileRepository doctorProfileRepository;
  private final DoctorDocumentRepository doctorDocumentRepository;
  private final UserService userService;
  private final MinioClient minioClient;

  @Value("${minio.bucket}")
  private String minioBucket;

  public DoctorProfile onboard(UUID userId, String role, DoctorOnboardRequest request) {
    userService.getOrCreate(userId, role);

    DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
        .orElseGet(() -> new DoctorProfile(
            UUID.randomUUID(),
            userId,
            null,
            null,
            null,
            null,
            LocalDateTime.now()
        ));

    if (request.getSpecialization() != null) {
      profile.setSpecialization(request.getSpecialization());
    }
    if (request.getLicenseNo() != null) {
      profile.setLicenseNo(request.getLicenseNo());
    }
    if (request.getExperienceYears() != null) {
      profile.setExperienceYears(request.getExperienceYears());
    }
    profile.setStatus("PENDING_VERIFICATION");

    return doctorProfileRepository.save(profile);
  }

  public Optional<DoctorDocsPresignResponse> presignDoc(
      UUID userId,
      DoctorDocsPresignRequest request
  ) throws Exception {
    DoctorProfile profile = doctorProfileRepository.findByUserId(userId).orElse(null);
    if (profile == null) {
      return Optional.empty();
    }

    String fileName = request.getFileName().trim();
    String s3Key = "doctor-docs/" + profile.getId() + "/" + UUID.randomUUID() + "-" + fileName;

    String uploadUrl = minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs.builder()
            .method(Method.PUT)
            .bucket(minioBucket)
            .object(s3Key)
            .expiry(15, TimeUnit.MINUTES)
            .build()
    );

    DoctorDocument document = new DoctorDocument(
        UUID.randomUUID(),
        profile.getId(),
        request.getDocType(),
        s3Key,
        "UPLOADED_PENDING_REVIEW",
        LocalDateTime.now()
    );
    doctorDocumentRepository.save(document);

    return Optional.of(new DoctorDocsPresignResponse(uploadUrl, s3Key));
  }
}
