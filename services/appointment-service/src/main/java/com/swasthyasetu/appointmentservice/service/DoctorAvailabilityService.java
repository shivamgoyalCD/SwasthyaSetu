package com.swasthyasetu.appointmentservice.service;

import com.swasthyasetu.appointmentservice.domain.DoctorAvailability;
import com.swasthyasetu.appointmentservice.dto.AvailabilitySlotRequest;
import com.swasthyasetu.appointmentservice.repository.DoctorAvailabilityRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {
  private final DoctorAvailabilityRepository doctorAvailabilityRepository;

  @Transactional
  public List<DoctorAvailability> replaceAll(UUID doctorId, List<AvailabilitySlotRequest> slots) {
    doctorAvailabilityRepository.deleteByDoctorId(doctorId);
    List<DoctorAvailability> availability = slots.stream()
        .map(slot -> new DoctorAvailability(
            UUID.randomUUID(),
            doctorId,
            slot.getDayOfWeek(),
            slot.getStartTime(),
            slot.getEndTime(),
            slot.getSlotMinutes(),
            slot.getBufferMinutes(),
            LocalDateTime.now()
        ))
        .toList();
    return doctorAvailabilityRepository.saveAll(availability);
  }

  @Transactional(readOnly = true)
  public List<DoctorAvailability> getDoctorAvailability(UUID doctorId) {
    return doctorAvailabilityRepository.findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(doctorId);
  }
}
