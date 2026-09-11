package com.medicare.practice.repository;

import com.medicare.practice.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByPatientIdOrderByIssueDateDesc(Long patientId);
    List<Prescription> findByDoctorIdOrderByIssueDateDesc(Long doctorId);
    List<Prescription> findByConsultationId(Long consultationId);
    long countByPatientId(Long patientId);
}
