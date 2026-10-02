package com.medicare.practice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicare.practice.dto.PrescriptionDTOs.*;
import com.medicare.practice.entity.*;
import com.medicare.practice.repository.ConsultationRepository;
import com.medicare.practice.repository.DoctorRepository;
import com.medicare.practice.repository.PatientRepository;
import com.medicare.practice.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private GeminiClient geminiClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescription prescription;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .build();

        doctor = Doctor.builder()
                .id(1L)
                .firstName("Sarah")
                .lastName("Jenkins")
                .build();

        prescription = Prescription.builder()
                .id(10L)
                .patient(patient)
                .doctor(doctor)
                .issueDate(LocalDate.now())
                .notes("Take after breakfast")
                .items(new ArrayList<>())
                .build();

        PrescriptionItem item = PrescriptionItem.builder()
                .id(100L)
                .prescription(prescription)
                .medicationName("Amlodipine Besylate")
                .dosage("5 mg")
                .frequency("Once daily")
                .duration("30 days")
                .instructions("Take after breakfast. Avoid grapefruit juice.")
                .build();

        prescription.getItems().add(item);
    }

    @Test
    @DisplayName("Should generate plain-language explanation using Gemini when enabled")
    void testExplainPrescription_ValidJsonResponse_MappedCorrectly() {
        when(prescriptionRepository.findById(10L)).thenReturn(Optional.of(prescription));
        when(geminiClient.isEnabled()).thenReturn(true);

        String jsonResponse = """
                ```json
                {
                  "overview": "This medication helps keep your blood pressure at a safe and healthy level.",
                  "medications": [
                    {
                      "medicationName": "Amlodipine Besylate",
                      "purpose": "Relaxes your blood vessels so blood flows more smoothly.",
                      "howToTake": "Take one 5mg tablet every morning with water after eating breakfast.",
                      "commonSideEffects": "Mild ankle swelling or lightheadedness when standing up quickly."
                    }
                  ],
                  "dietaryAndLifestyleAdvice": "Do not drink grapefruit juice or eat grapefruit while on this medicine.",
                  "whenToCallDoctor": "Call if you have sudden severe dizziness or chest discomfort."
                }
                ```
                """;

        when(geminiClient.generateContent(anyString())).thenReturn(jsonResponse);

        PrescriptionExplanationDTO result = prescriptionService.explainPrescription(10L);

        assertNotNull(result);
        assertTrue(result.isAiGenerated());
        assertEquals(10L, result.getPrescriptionId());
        assertEquals("This medication helps keep your blood pressure at a safe and healthy level.", result.getOverview());
        assertEquals(1, result.getMedications().size());
        assertEquals("Amlodipine Besylate", result.getMedications().get(0).getMedicationName());
        assertEquals("Relaxes your blood vessels so blood flows more smoothly.", result.getMedications().get(0).getPurpose());
        assertTrue(result.getDietaryAndLifestyleAdvice().contains("grapefruit"));

        verify(geminiClient, times(1)).generateContent(anyString());
    }

    @Test
    @DisplayName("Should fallback to deterministic plain-language explanation when API fails")
    void testExplainPrescription_ApiException_FallsBackToDefault() {
        when(prescriptionRepository.findById(10L)).thenReturn(Optional.of(prescription));
        when(geminiClient.isEnabled()).thenReturn(true);
        when(geminiClient.generateContent(anyString())).thenThrow(new RuntimeException("Gemini Service Unavailable"));

        PrescriptionExplanationDTO result = prescriptionService.explainPrescription(10L);

        assertNotNull(result);
        assertFalse(result.isAiGenerated());
        assertEquals(10L, result.getPrescriptionId());
        assertEquals(1, result.getMedications().size());
        assertEquals("Amlodipine Besylate", result.getMedications().get(0).getMedicationName());
        assertTrue(result.getMedications().get(0).getHowToTake().contains("5 mg"));

        verify(geminiClient, times(1)).generateContent(anyString());
    }

    @Test
    @DisplayName("Should fallback without calling API when Gemini is not enabled")
    void testExplainPrescription_KeyMissing_FallsBackDirectly() {
        when(prescriptionRepository.findById(10L)).thenReturn(Optional.of(prescription));
        when(geminiClient.isEnabled()).thenReturn(false);

        PrescriptionExplanationDTO result = prescriptionService.explainPrescription(10L);

        assertNotNull(result);
        assertFalse(result.isAiGenerated());
        assertEquals(10L, result.getPrescriptionId());

        verify(geminiClient, never()).generateContent(any());
    }
}
