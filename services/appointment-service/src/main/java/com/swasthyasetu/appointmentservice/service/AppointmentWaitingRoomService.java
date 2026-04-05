package com.swasthyasetu.appointmentservice.service;

import com.swasthyasetu.appointmentservice.domain.Appointment;
import com.swasthyasetu.appointmentservice.dto.DoctorQueueItemResponse;
import com.swasthyasetu.appointmentservice.repository.AppointmentRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentWaitingRoomService {
  private static final String BOOKED_STATUS = "BOOKED";
  private static final String ONLINE_STATUS = "ONLINE";
  private static final Duration PRESENCE_TTL = Duration.ofHours(2);

  private final AppointmentRepository appointmentRepository;
  private final StringRedisTemplate stringRedisTemplate;

  public Optional<Appointment> joinWaitingRoom(UUID appointmentId, UUID patientId) {
    Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
    if (appointment.isEmpty()) {
      return Optional.empty();
    }

    Appointment existing = appointment.get();
    if (!patientId.equals(existing.getPatientId())) {
      throw new AppointmentWaitingRoomForbiddenException("Only the booking patient can join the waiting room");
    }
    if (!BOOKED_STATUS.equals(existing.getStatus())) {
      throw new AppointmentWaitingRoomInvalidStateException("Only booked appointments can join the waiting room");
    }

    stringRedisTemplate.opsForValue().set(presenceKey(existing.getId()), ONLINE_STATUS, PRESENCE_TTL);
    stringRedisTemplate.opsForZSet().add(
        queueKey(existing.getDoctorId(), existing.getStartTs().toLocalDate()),
        existing.getId().toString(),
        toEpochScore(existing)
    );

    return Optional.of(existing);
  }

  public List<DoctorQueueItemResponse> getDoctorQueue(UUID doctorId, LocalDate date) {
    Set<ZSetOperations.TypedTuple<String>> queueEntries = stringRedisTemplate.opsForZSet()
        .rangeWithScores(queueKey(doctorId, date), 0, -1);
    if (queueEntries == null || queueEntries.isEmpty()) {
      return List.of();
    }

    return queueEntries.stream()
        .map(entry -> new DoctorQueueItemResponse(
            UUID.fromString(entry.getValue()),
            Boolean.TRUE.equals(stringRedisTemplate.hasKey(presenceKey(UUID.fromString(entry.getValue()))))
        ))
        .toList();
  }

  private double toEpochScore(Appointment appointment) {
    return appointment.getStartTs().atZone(ZoneId.systemDefault()).toEpochSecond();
  }

  private String presenceKey(UUID appointmentId) {
    return "presence:appointment:" + appointmentId;
  }

  private String queueKey(UUID doctorId, LocalDate date) {
    return "queue:doctor:" + doctorId + ":" + date;
  }
}
