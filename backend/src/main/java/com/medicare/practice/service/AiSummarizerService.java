package com.medicare.practice.service;

import com.medicare.practice.dto.ConsultationDTOs.AiSummaryDTO;
import com.medicare.practice.entity.Consultation;
import com.medicare.practice.entity.Patient;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.ConsultationRepository;
import com.medicare.practice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSummarizerService {

    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openAiModel;

    @Transactional(readOnly = true)
    public AiSummaryDTO summarizePatientHistory(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + patientId));

        List<Consultation> consultations = consultationRepository.findByPatientIdOrderByConsultationDateDesc(patientId);

        if (consultations.isEmpty()) {
            return AiSummaryDTO.builder()
                    .patientId(patient.getId())
                    .patientName(patient.getFullName())
                    .totalConsultationsAnalyzed(0)
                    .chiefComplaintsSummary("No prior consultation records found for this patient.")
                    .diagnosisHistory("None on file.")
                    .treatmentsSummary("None on file.")
                    .followUpDirectives("Initial consultation pending.")
                    .rawExecutiveSummary("Patient " + patient.getFullName() + " has no prior clinical records in this practice.")
                    .isAiGenerated(false)
                    .build();
        }

        // Aggregate key medical datapoints chronologically
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        String complaintsSummary = consultations.stream()
                .map(c -> "• " + c.getConsultationDate().format(dtf) + ": " + c.getSymptoms())
                .collect(Collectors.joining("\n"));

        String diagnosisHistory = consultations.stream()
                .map(c -> "• " + c.getConsultationDate().format(dtf) + " (Dr. " + c.getDoctor().getLastName() + "): " + c.getDiagnosis())
                .collect(Collectors.joining("\n"));

        String treatmentsSummary = consultations.stream()
                .filter(c -> c.getTreatmentPlan() != null && !c.getTreatmentPlan().isBlank())
                .map(c -> "• " + c.getConsultationDate().format(dtf) + ": " + c.getTreatmentPlan())
                .collect(Collectors.joining("\n"));

        String followUps = consultations.stream()
                .filter(c -> c.getFollowUpDate() != null)
                .map(c -> "• Next Follow-up: " + c.getFollowUpDate().toString() + (c.getNotes() != null ? " (" + c.getNotes() + ")" : ""))
                .collect(Collectors.joining("\n"));

        if (followUps.isBlank()) {
            followUps = "No specific pending follow-ups noted.";
        }

        String rawSummary = String.format(
                "Clinical Summary for %s (DOB: %s, Blood: %s):\n" +
                "Total consultations: %d.\n" +
                "Primary past diagnoses: %s.\n" +
                "Most recent complaint: %s on %s.",
                patient.getFullName(),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "N/A",
                patient.getBloodGroup() != null ? patient.getBloodGroup() : "N/A",
                consultations.size(),
                consultations.get(0).getDiagnosis(),
                consultations.get(0).getSymptoms(),
                consultations.get(0).getConsultationDate().format(dtf)
        );

        return AiSummaryDTO.builder()
                .patientId(patient.getId())
                .patientName(patient.getFullName())
                .totalConsultationsAnalyzed(consultations.size())
                .chiefComplaintsSummary(complaintsSummary)
                .diagnosisHistory(diagnosisHistory)
                .treatmentsSummary(treatmentsSummary.isBlank() ? "Standard clinical observation." : treatmentsSummary)
                .followUpDirectives(followUps)
                .rawExecutiveSummary(rawSummary)
                .isAiGenerated(true)
                .build();
    }
}
