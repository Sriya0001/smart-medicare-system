package com.medicare.practice.repository;

import com.medicare.practice.entity.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorId(Long doctorId);
    List<DoctorAvailability> findByDoctorIdAndActiveTrue(Long doctorId);
    List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndActiveTrue(Long doctorId, DayOfWeek dayOfWeek);
    Optional<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
}
