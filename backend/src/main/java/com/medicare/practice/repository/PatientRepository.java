package com.medicare.practice.repository;

import com.medicare.practice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUserEmail(String email);

    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.phoneNumber LIKE CONCAT('%', :query, '%')")
    List<Patient> searchPatients(@Param("query") String query);

    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
    List<Patient> findPatientsByDoctorId(@Param("doctorId") Long doctorId);
}
