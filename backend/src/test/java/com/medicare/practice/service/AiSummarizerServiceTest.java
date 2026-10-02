package com.medicare.practice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicare.practice.dto.ConsultationDTOs.AiSummaryDTO;
import com.medicare.practice.entity.*;
import com.medicare.practice.repository.ConsultationRepository;
import com.medicare.practice.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AiSummarizerServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private GeminiClient geminiClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AiSummarizerService aiSummarizerService;

    private Patient patient;
    private Consultation consultation;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("Peter")
                .lastName("Parker")
                .dateOfBirth(LocalDate.of(1995, 8, 10))
                .bloodGroup("O+")
                .build();

        Doctor doctor = Doctor.builder()
                .id(1L)
                .firstName("Gregory")
                .lastName("House")
                .specialty("Diagnostics")
                .build();

        consultation = Consultation.builder()
                .id(100L)
                .patient(patient)
                .doctor(doctor)
                .consultationDate(LocalDateTime.of(2026, 9, 15, 10, 0))
                .symptoms("Persistent dry cough and mild fatigue")
                .diagnosis("Acute Bronchitis")
                .treatmentPlan("Hydration, rest, and 5-day course of Azithromycin")
                .notes("Lungs clear bilaterally")
                .followUpDate(LocalDate.of(2026, 9, 25))
                .build();
    }

    @Test
    @DisplayName("Valid JSON response from Gemini is parsed and mapped correctly to AiSummaryDTO")
    void testSummarizePatientHistory_ValidJsonResponse_MappedCorrectly() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(consultationRepository.findByPatientIdOrderByConsultationDateDesc(1L)).thenReturn(List.of(consultation));
        when(geminiClient.isEnabled()).thenReturn(true);

        String jsonResponse = """
                ```json
                {
                  "chiefComplaintsSummary": "Dry cough and mild fatigue lasting two weeks",
                  "diagnosisHistory": "Acute Bronchitis (Sep 15, 2026)",
                  "treatmentsSummary": "Azithromycin 5-day regimen with hydration and rest",
                  "followUpDirectives": "Scheduled follow-up on Sep 25, 2026",
                  "rawExecutiveSummary": "Patient presented with upper respiratory symptoms diagnosed as acute bronchitis."
                }
                ```
                """;

        when(geminiClient.generateContent(anyString())).thenReturn(jsonResponse);

        AiSummaryDTO result = aiSummarizerService.summarizePatientHistory(1L);

        assertNotNull(result);
        assertTrue(result.isAiGenerated());
        assertEquals(1L, result.getPatientId());
        assertEquals("Peter Parker", result.getPatientName());
        assertEquals(1, result.getTotalConsultationsAnalyzed());
        assertEquals("Dry cough and mild fatigue lasting two weeks", result.getChiefComplaintsSummary());
        assertEquals("Acute Bronchitis (Sep 15, 2026)", result.getDiagnosisHistory());
        assertEquals("Azithromycin 5-day regimen with hydration and rest", result.getTreatmentsSummary());
        assertEquals("Scheduled follow-up on Sep 25, 2026", result.getFollowUpDirectives());
        assertEquals("Patient presented with upper respiratory symptoms diagnosed as acute bronchitis.", result.getRawExecutiveSummary());

        verify(geminiClient, times(1)).generateContent(anyString());
    }

    @Test
    @DisplayName("API throws exception -> falls back to deterministic default summary")
    void testSummarizePatientHistory_ApiException_FallsBackToDefaultSummary() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(consultationRepository.findByPatientIdOrderByConsultationDateDesc(1L)).thenReturn(List.of(consultation));
        when(geminiClient.isEnabled()).thenReturn(true);
        when(geminiClient.generateContent(anyString())).thenThrow(new RuntimeException("API Connection Timeout (HTTP 504)"));

        AiSummaryDTO result = aiSummarizerService.summarizePatientHistory(1L);

        assertNotNull(result);
        assertFalse(result.isAiGenerated());
        assertEquals(1L, result.getPatientId());
        assertEquals("Peter Parker", result.getPatientName());
        assertEquals(1, result.getTotalConsultationsAnalyzed());
        assertTrue(result.getChiefComplaintsSummary().contains("Persistent dry cough and mild fatigue"));
        assertTrue(result.getDiagnosisHistory().contains("Acute Bronchitis"));
        assertTrue(result.getTreatmentsSummary().contains("Hydration, rest, and 5-day course of Azithromycin"));
        assertTrue(result.getFollowUpDirectives().contains("2026-09-25"));
        assertTrue(result.getRawExecutiveSummary().contains("Clinical Summary for Peter Parker"));

        verify(geminiClient, times(1)).generateContent(anyString());
    }

    @Test
    @DisplayName("Gemini API key missing (isEnabled=false) -> falls back without calling API")
    void testSummarizePatientHistory_KeyMissing_FallsBackWithoutCallingApi() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(consultationRepository.findByPatientIdOrderByConsultationDateDesc(1L)).thenReturn(List.of(consultation));
        when(geminiClient.isEnabled()).thenReturn(false);

        AiSummaryDTO result = aiSummarizerService.summarizePatientHistory(1L);

        assertNotNull(result);
        assertFalse(result.isAiGenerated());
        assertEquals(1L, result.getPatientId());
        assertEquals(1, result.getTotalConsultationsAnalyzed());
        assertTrue(result.getDiagnosisHistory().contains("Acute Bronchitis"));

        verify(geminiClient, never()).generateContent(any());
    }

    @Test
    @DisplayName("No past consultations -> returns empty state without calling API")
    void testSummarizePatientHistory_NoConsultations_ReturnsEmptyState() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(consultationRepository.findByPatientIdOrderByConsultationDateDesc(1L)).thenReturn(Collections.emptyList());

        AiSummaryDTO result = aiSummarizerService.summarizePatientHistory(1L);

        assertNotNull(result);
        assertFalse(result.isAiGenerated());
        assertEquals(0, result.getTotalConsultationsAnalyzed());
        assertEquals("No prior consultation records found for this patient.", result.getChiefComplaintsSummary());

        verify(geminiClient, never()).isEnabled();
        verify(geminiClient, never()).generateContent(any());
    }
}
