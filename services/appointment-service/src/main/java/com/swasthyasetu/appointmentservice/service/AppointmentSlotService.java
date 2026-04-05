package com.swasthyasetu.appointmentservice.service;

import com.swasthyasetu.appointmentservice.domain.Appointment;
import com.swasthyasetu.appointmentservice.domain.DoctorAvailability;
import com.swasthyasetu.appointmentservice.dto.AppointmentSlotResponse;
import com.swasthyasetu.appointmentservice.repository.AppointmentRepository;
import com.swasthyasetu.appointmentservice.repository.DoctorAvailabilityRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentSlotService {
  private static final String BOOKED_STATUS = "BOOKED";

  private final DoctorAvailabilityRepository doctorAvailabilityRepository;
  private final AppointmentRepository appointmentRepository;

  @Transactional(readOnly = true)
  public List<AppointmentSlotResponse> getAvailableSlots(UUID doctorId, LocalDate date) {
    List<AppointmentSlotResponse> generatedSlots = getGeneratedSlots(doctorId, date);
    if (generatedSlots.isEmpty()) {
      return List.of();
    }

    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime nextDayStart = date.plusDays(1).atStartOfDay();
    Set<LocalDateTime> bookedStartTimes = appointmentRepository
        .findByDoctorIdAndStatusAndStartTsGreaterThanEqualAndStartTsLessThanOrderByStartTsAsc(
            doctorId,
            BOOKED_STATUS,
            dayStart,
            nextDayStart
        )
        .stream()
        .map(Appointment::getStartTs)
        .collect(Collectors.toSet());

    return generatedSlots.stream()
        .filter(slot -> !bookedStartTimes.contains(slot.getStartTs()))
        .toList();
  }

  @Transactional(readOnly = true)
  public Optional<AppointmentSlotResponse> getSlotFromAvailability(UUID doctorId, LocalDateTime startTs) {
    return getGeneratedSlots(doctorId, startTs.toLocalDate()).stream()
        .filter(slot -> slot.getStartTs().equals(startTs))
        .findFirst();
  }

  private List<AppointmentSlotResponse> getGeneratedSlots(UUID doctorId, LocalDate date) {
    int weekday = toStoredDayOfWeek(date);
    List<DoctorAvailability> availability = doctorAvailabilityRepository
        .findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(doctorId, weekday);

    Map<LocalDateTime, AppointmentSlotResponse> generatedSlots = new LinkedHashMap<>();
    for (DoctorAvailability window : availability) {
      generateSlotsForWindow(date, window, generatedSlots);
    }
    return generatedSlots.values().stream().toList();
  }

  private void generateSlotsForWindow(
      LocalDate date,
      DoctorAvailability window,
      Map<LocalDateTime, AppointmentSlotResponse> generatedSlots
  ) {
    LocalDateTime currentStart = LocalDateTime.of(date, window.getStartTime());
    LocalDateTime windowEnd = LocalDateTime.of(date, window.getEndTime());

    while (!currentStart.plusMinutes(window.getSlotMinutes()).isAfter(windowEnd)) {
      LocalDateTime currentEnd = currentStart.plusMinutes(window.getSlotMinutes());
      generatedSlots.putIfAbsent(currentStart, new AppointmentSlotResponse(currentStart, currentEnd));
      currentStart = currentEnd.plusMinutes(window.getBufferMinutes());
    }
  }

  private int toStoredDayOfWeek(LocalDate date) {
    return date.getDayOfWeek().getValue() % 7;
  }
}
