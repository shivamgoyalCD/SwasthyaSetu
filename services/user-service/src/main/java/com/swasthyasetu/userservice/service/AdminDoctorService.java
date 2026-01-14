package com.swasthyasetu.userservice.service;

import com.swasthyasetu.userservice.domain.DoctorProfile;
import com.swasthyasetu.userservice.repository.DoctorProfileRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminDoctorService {
  private final DoctorProfileRepository doctorProfileRepository;

  public Optional<DoctorProfile> updateStatus(UUID doctorId, String status) {
    return doctorProfileRepository.findById(doctorId).map(profile -> {
      profile.setStatus(status);
      return doctorProfileRepository.save(profile);
    });
  }

  public List<DoctorProfile> listPending() {
    return doctorProfileRepository.findByStatus("PENDING_VERIFICATION");
  }
}
