package com.swasthyasetu.appointmentservice.repository;

import com.swasthyasetu.appointmentservice.domain.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
  List<Appointment> findByDoctorIdAndStatusAndStartTsGreaterThanEqualAndStartTsLessThanOrderByStartTsAsc(
      UUID doctorId,
      String status,
      LocalDateTime startTs,
      LocalDateTime endTs
  );
}
