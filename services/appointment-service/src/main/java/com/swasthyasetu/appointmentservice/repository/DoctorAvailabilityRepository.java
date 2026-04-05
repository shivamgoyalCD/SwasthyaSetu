package com.swasthyasetu.appointmentservice.repository;

import com.swasthyasetu.appointmentservice.domain.DoctorAvailability;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, UUID> {
  void deleteByDoctorId(UUID doctorId);

  List<DoctorAvailability> findByDoctorIdAndDayOfWeekOrderByStartTimeAsc(UUID doctorId, int dayOfWeek);

  List<DoctorAvailability> findByDoctorIdOrderByDayOfWeekAscStartTimeAsc(UUID doctorId);
}
