package com.swasthyasetu.appointmentservice.service;

import com.swasthyasetu.appointmentservice.domain.Appointment;
import com.swasthyasetu.appointmentservice.dto.AppointmentSlotResponse;
import com.swasthyasetu.appointmentservice.repository.AppointmentRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentBookingService {
  private static final String BOOKED_STATUS = "BOOKED";
  private static final String CANCELLED_STATUS = "CANCELLED";
  private static final String DOCTOR_SLOT_UNIQUE_CONSTRAINT = "uk_appointments_doctor_start_ts";

  private final AppointmentSlotService appointmentSlotService;
  private final AppointmentRepository appointmentRepository;

  @Transactional
  public Optional<Appointment> book(UUID patientId, UUID doctorId, LocalDateTime startTs) {
    Optional<AppointmentSlotResponse> slot = appointmentSlotService.getSlotFromAvailability(doctorId, startTs);
    if (slot.isEmpty()) {
      return Optional.empty();
    }

    Appointment appointment = new Appointment(
        UUID.randomUUID(),
        patientId,
        doctorId,
        startTs,
        slot.get().getEndTs(),
        BOOKED_STATUS,
        LocalDateTime.now()
    );

    try {
      return Optional.of(appointmentRepository.saveAndFlush(appointment));
    } catch (DataIntegrityViolationException ex) {
      if (isDoctorSlotConflict(ex)) {
        throw new AppointmentBookingConflictException("Appointment slot already booked", ex);
      }
      throw ex;
    }
  }

  @Transactional
  public Optional<Appointment> cancel(UUID appointmentId, UUID actorId, String actorRole) {
    Optional<Appointment> appointment = appointmentRepository.findById(appointmentId);
    if (appointment.isEmpty()) {
      return Optional.empty();
    }

    Appointment existing = appointment.get();
    boolean canCancel = ("PATIENT".equals(actorRole) && actorId.equals(existing.getPatientId()))
        || ("DOCTOR".equals(actorRole) && actorId.equals(existing.getDoctorId()));
    if (!canCancel) {
      throw new AppointmentCancellationForbiddenException("Not allowed to cancel this appointment");
    }

    existing.setStatus(CANCELLED_STATUS);
    return Optional.of(appointmentRepository.save(existing));
  }

  private boolean isDoctorSlotConflict(Throwable throwable) {
    Throwable current = throwable;
    while (current != null) {
      String message = current.getMessage();
      if (message != null && message.contains(DOCTOR_SLOT_UNIQUE_CONSTRAINT)) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }
}
