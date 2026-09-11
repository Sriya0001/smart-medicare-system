package com.medicare.practice.repository;

import com.medicare.practice.entity.Appointment;
import com.medicare.practice.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate appointmentDate);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusIn(
            Long doctorId, LocalDate appointmentDate, List<AppointmentStatus> statuses);

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(
            Long doctorId, LocalDate appointmentDate, AppointmentStatus status);

    List<Appointment> findByPatientIdAndStatusIn(Long patientId, List<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status != 'CANCELLED'")
    List<Appointment> findActiveDoctorAppointmentsForDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date AND a.status != 'CANCELLED' AND " +
           "((a.startTime < :endTime AND a.endTime > :startTime))")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :date ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findUpcomingDoctorAppointments(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :date AND a.status != 'CANCELLED' ORDER BY a.appointmentDate ASC, a.startTime ASC")
    List<Appointment> findUpcomingPatientAppointments(@Param("patientId") Long patientId, @Param("date") LocalDate date);

    long countByAppointmentDate(LocalDate date);

    long countByStatus(AppointmentStatus status);

    long countByDoctorIdAndStatus(Long doctorId, AppointmentStatus status);

    long countByPatientId(Long patientId);

    long countByDoctorId(Long doctorId);
}
