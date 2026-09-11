package com.medicare.practice.repository;

import com.medicare.practice.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Optional<Consultation> findByAppointmentId(Long appointmentId);
    List<Consultation> findByPatientIdOrderByConsultationDateDesc(Long patientId);
    List<Consultation> findByDoctorIdOrderByConsultationDateDesc(Long doctorId);
    long countByDoctorId(Long doctorId);
    long countByPatientId(Long patientId);
}
