package com.medicare.practice.service;

import com.medicare.practice.dto.ConsultationDTOs.*;
import com.medicare.practice.dto.PrescriptionDTOs;
import com.medicare.practice.entity.*;
import com.medicare.practice.exception.BadRequestException;
import com.medicare.practice.exception.ConflictException;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.AppointmentRepository;
import com.medicare.practice.repository.ConsultationRepository;
import com.medicare.practice.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getAllConsultations() {
        return consultationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultationDTO getConsultationById(Long id) {
        Consultation consultation = consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with ID: " + id));
        return mapToDTO(consultation);
    }

    @Transactional(readOnly = true)
    public ConsultationDTO getConsultationByAppointmentId(Long appointmentId) {
        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found for appointment ID: " + appointmentId));
        return mapToDTO(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getConsultationsByPatientId(Long patientId) {
        return consultationRepository.findByPatientIdOrderByConsultationDateDesc(patientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ConsultationDTO> getConsultationsByDoctorId(Long doctorId) {
        return consultationRepository.findByDoctorIdOrderByConsultationDateDesc(doctorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConsultationDTO createConsultation(ConsultationCreateRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + request.getAppointmentId()));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot create a consultation record for a cancelled appointment.");
        }

        if (consultationRepository.findByAppointmentId(request.getAppointmentId()).isPresent()) {
            throw new ConflictException("A consultation note already exists for appointment #" + request.getAppointmentId());
        }

        Consultation consultation = Consultation.builder()
                .appointment(appointment)
                .patient(appointment.getPatient())
                .doctor(appointment.getDoctor())
                .consultationDate(LocalDateTime.now())
                .symptoms(request.getSymptoms())
                .diagnosis(request.getDiagnosis())
                .treatmentPlan(request.getTreatmentPlan())
                .notes(request.getNotes())
                .followUpDate(request.getFollowUpDate())
                .prescriptions(new ArrayList<>())
                .build();

        consultation = consultationRepository.save(consultation);

        // If prescription items were supplied, create associated prescription
        if (request.getPrescriptionItems() != null && !request.getPrescriptionItems().isEmpty()) {
            Prescription prescription = Prescription.builder()
                    .consultation(consultation)
                    .patient(appointment.getPatient())
                    .doctor(appointment.getDoctor())
                    .issueDate(LocalDate.now())
                    .notes("Generated during consultation on " + LocalDate.now())
                    .items(new ArrayList<>())
                    .build();

            for (PrescriptionDTOs.PrescriptionItemRequest itemReq : request.getPrescriptionItems()) {
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
            consultation.getPrescriptions().add(prescription);
        }

        // Mark appointment as COMPLETED automatically
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        log.info("Completed appointment #{} and created consultation note #{}", appointment.getId(), consultation.getId());
        return mapToDTO(consultation);
    }

    public ConsultationDTO mapToDTO(Consultation c) {
        List<PrescriptionDTOs.PrescriptionDTO> prescriptionDTOs = null;
        if (c.getPrescriptions() != null) {
            prescriptionDTOs = c.getPrescriptions().stream()
                    .map(p -> PrescriptionDTOs.PrescriptionDTO.builder()
                            .id(p.getId())
                            .consultationId(c.getId())
                            .patientId(p.getPatient().getId())
                            .patientName(p.getPatient().getFullName())
                            .doctorId(p.getDoctor().getId())
                            .doctorName(p.getDoctor().getFullName())
                            .issueDate(p.getIssueDate())
                            .notes(p.getNotes())
                            .items(p.getItems() != null ? p.getItems().stream()
                                    .map(item -> PrescriptionDTOs.PrescriptionItemDTO.builder()
                                            .id(item.getId())
                                            .medicationName(item.getMedicationName())
                                            .dosage(item.getDosage())
                                            .frequency(item.getFrequency())
                                            .duration(item.getDuration())
                                            .instructions(item.getInstructions())
                                            .build())
                                    .collect(Collectors.toList()) : List.of())
                            .createdAt(p.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        return ConsultationDTO.builder()
                .id(c.getId())
                .appointmentId(c.getAppointment().getId())
                .patientId(c.getPatient().getId())
                .patientName(c.getPatient().getFullName())
                .doctorId(c.getDoctor().getId())
                .doctorName(c.getDoctor().getFullName())
                .doctorSpecialty(c.getDoctor().getSpecialty())
                .consultationDate(c.getConsultationDate())
                .symptoms(c.getSymptoms())
                .diagnosis(c.getDiagnosis())
                .treatmentPlan(c.getTreatmentPlan())
                .notes(c.getNotes())
                .followUpDate(c.getFollowUpDate())
                .prescriptions(prescriptionDTOs)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
