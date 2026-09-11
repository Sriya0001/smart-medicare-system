package com.medicare.practice.service;

import com.medicare.practice.dto.ConsultationDTOs.*;
import com.medicare.practice.dto.PrescriptionDTOs;
import com.medicare.practice.entity.*;
import com.medicare.practice.exception.ConflictException;
import com.medicare.practice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class ConsultationServiceTest {

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    private Appointment appointment;

    @BeforeEach
    void setUp() {
        consultationRepository.deleteAll();
        appointmentRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        User docUser = userRepository.save(User.builder()
                .email("dr.house@test.com")
                .password("hash")
                .role(Role.ROLE_DOCTOR)
                .enabled(true)
                .build());

        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(docUser)
                .firstName("Gregory")
                .lastName("House")
                .specialty("Diagnostics")
                .licenseNumber("MD-999")
                .active(true)
                .build());

        User patUser = userRepository.save(User.builder()
                .email("patient.zero@test.com")
                .password("hash")
                .role(Role.ROLE_PATIENT)
                .enabled(true)
                .build());

        Patient patient = patientRepository.save(Patient.builder()
                .user(patUser)
                .firstName("Peter")
                .lastName("Parker")
                .build());

        appointment = appointmentRepository.save(Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .appointmentDate(LocalDate.now())
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .status(AppointmentStatus.CONFIRMED)
                .reason("Persistent cough and fatigue")
                .build());
    }

    @Test
    @DisplayName("Should create consultation notes, attach prescription, and transition appointment to COMPLETED")
    void testCreateConsultation_Success() {
        PrescriptionDTOs.PrescriptionItemRequest item = PrescriptionDTOs.PrescriptionItemRequest.builder()
                .medicationName("Amoxicillin")
                .dosage("500mg")
                .frequency("Three times daily")
                .duration("10 days")
                .instructions("Take with full glass of water after food")
                .build();

        ConsultationCreateRequest request = ConsultationCreateRequest.builder()
                .appointmentId(appointment.getId())
                .symptoms("Productive cough for 5 days, low grade fever")
                .diagnosis("Acute Bronchitis")
                .treatmentPlan("Hydration, rest, course of oral antibiotics")
                .notes("Lungs clear bilaterally with slight wheezing on right base")
                .followUpDate(LocalDate.now().plusDays(10))
                .prescriptionItems(List.of(item))
                .build();

        ConsultationDTO result = consultationService.createConsultation(request);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Acute Bronchitis", result.getDiagnosis());
        assertNotNull(result.getPrescriptions());
        assertEquals(1, result.getPrescriptions().size());
        assertEquals(1, result.getPrescriptions().get(0).getItems().size());
        assertEquals("Amoxicillin", result.getPrescriptions().get(0).getItems().get(0).getMedicationName());

        // Verify appointment status updated to COMPLETED
        Appointment updatedAppt = appointmentRepository.findById(appointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.COMPLETED, updatedAppt.getStatus());
    }

    @Test
    @DisplayName("Should prevent creating duplicate consultation notes for same appointment")
    void testCreateDuplicateConsultation_ThrowsConflictException() {
        ConsultationCreateRequest request = ConsultationCreateRequest.builder()
                .appointmentId(appointment.getId())
                .symptoms("Headache")
                .diagnosis("Tension Headache")
                .build();
        consultationService.createConsultation(request);

        assertThrows(ConflictException.class, () -> consultationService.createConsultation(request));
    }
}
