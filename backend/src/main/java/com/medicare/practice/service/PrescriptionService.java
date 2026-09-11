package com.medicare.practice.service;

import com.medicare.practice.dto.PrescriptionDTOs.*;
import com.medicare.practice.entity.*;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.ConsultationRepository;
import com.medicare.practice.repository.DoctorRepository;
import com.medicare.practice.repository.PatientRepository;
import com.medicare.practice.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationRepository consultationRepository;

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getAllPrescriptions() {
        return prescriptionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PrescriptionDTO getPrescriptionById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + id));
        return mapToDTO(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByIssueDateDesc(patientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionDTO> getPrescriptionsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByIssueDateDesc(doctorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PrescriptionDTO createPrescription(Long doctorId, PrescriptionCreateRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + doctorId));

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        Consultation consultation = null;
        if (request.getConsultationId() != null) {
            consultation = consultationRepository.findById(request.getConsultationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with ID: " + request.getConsultationId()));
        }

        Prescription prescription = Prescription.builder()
                .doctor(doctor)
                .patient(patient)
                .consultation(consultation)
                .issueDate(LocalDate.now())
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        for (PrescriptionItemRequest itemReq : request.getItems()) {
            PrescriptionItem item = PrescriptionItem.builder()
                    .prescription(prescription)
                    .medicationName(itemReq.getMedicationName())
                    .dosage(itemReq.getDosage())
                    .frequency(itemReq.getFrequency())
                    .duration(itemReq.getDuration())
                    .instructions(itemReq.getInstructions())
                    .build();
            prescription.getItems().add(item);
        }

        prescription = prescriptionRepository.save(prescription);
        return mapToDTO(prescription);
    }

    public PrescriptionDTO mapToDTO(Prescription p) {
        return PrescriptionDTO.builder()
                .id(p.getId())
                .consultationId(p.getConsultation() != null ? p.getConsultation().getId() : null)
                .patientId(p.getPatient().getId())
                .patientName(p.getPatient().getFullName())
                .doctorId(p.getDoctor().getId())
                .doctorName(p.getDoctor().getFullName())
                .issueDate(p.getIssueDate())
                .notes(p.getNotes())
                .items(p.getItems() != null ? p.getItems().stream()
                        .map(item -> PrescriptionItemDTO.builder()
                                .id(item.getId())
                                .medicationName(item.getMedicationName())
                                .dosage(item.getDosage())
                                .frequency(item.getFrequency())
                                .duration(item.getDuration())
                                .instructions(item.getInstructions())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
