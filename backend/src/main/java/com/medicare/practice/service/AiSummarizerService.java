package com.medicare.practice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicare.practice.dto.ConsultationDTOs.AiSummaryDTO;
import com.medicare.practice.entity.Consultation;
import com.medicare.practice.entity.Patient;
import com.medicare.practice.entity.Prescription;
import com.medicare.practice.entity.PrescriptionItem;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.ConsultationRepository;
import com.medicare.practice.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

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

        if (geminiClient.isEnabled()) {
            try {
                String prompt = buildPrompt(consultations);
                String rawResponse = geminiClient.generateContent(prompt);
                return parseAndBuildSummary(patient, consultations, rawResponse);
            } catch (Exception e) {
                log.warn("Gemini AI summarization unavailable or failed, falling back to local deterministic summary. Reason: {}", e.getMessage());
            }
        } else {
            log.info("Gemini API is not enabled/configured. Using local deterministic summary fallback.");
        }

        return buildLocalFallbackSummary(patient, consultations);
    }

    private String buildPrompt(List<Consultation> consultations) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert clinical documentation AI assistant. Analyze the following patient consultation history and generate a structured clinical summary.\n\n");
        sb.append("CLINICAL CONSULTATION RECORDS (Total: ").append(consultations.size()).append("):\n");

        for (int i = 0; i < consultations.size(); i++) {
            Consultation c = consultations.get(i);
            sb.append(String.format("\n--- Record %d [%s] ---\n", i + 1, c.getConsultationDate().format(dtf)));
            sb.append("Doctor Specialty: ").append(c.getDoctor() != null ? c.getDoctor().getSpecialty() : "General Practice").append("\n");
            sb.append("Symptoms / Chief Complaint: ").append(c.getSymptoms() != null ? c.getSymptoms() : "None noted").append("\n");
            sb.append("Diagnosis: ").append(c.getDiagnosis() != null ? c.getDiagnosis() : "None noted").append("\n");
            if (c.getTreatmentPlan() != null && !c.getTreatmentPlan().isBlank()) {
                sb.append("Treatment Plan: ").append(c.getTreatmentPlan()).append("\n");
            }
            if (c.getNotes() != null && !c.getNotes().isBlank()) {
                sb.append("Clinical Notes: ").append(c.getNotes()).append("\n");
            }
            if (c.getFollowUpDate() != null) {
                sb.append("Scheduled Follow-up Date: ").append(c.getFollowUpDate().toString()).append("\n");
            }
            if (c.getPrescriptions() != null && !c.getPrescriptions().isEmpty()) {
                sb.append("Prescriptions:\n");
                for (Prescription p : c.getPrescriptions()) {
                    if (p.getItems() != null) {
                        for (PrescriptionItem item : p.getItems()) {
                            sb.append(String.format("  - Medication: %s, Dosage: %s, Frequency: %s, Duration: %s%s\n",
                                    item.getMedicationName(),
                                    item.getDosage() != null ? item.getDosage() : "N/A",
                                    item.getFrequency() != null ? item.getFrequency() : "N/A",
                                    item.getDuration() != null ? item.getDuration() : "N/A",
                                    (item.getInstructions() != null && !item.getInstructions().isBlank()) ? ", Instructions: " + item.getInstructions() : ""
                            ));
                        }
                    }
                }
            }
        }

        sb.append("\nINSTRUCTIONS:\n");
        sb.append("Return ONLY a valid JSON object (no code blocks, no markdown formatting, no explanation) with the following exact keys:\n");
        sb.append("{\n");
        sb.append("  \"chiefComplaintsSummary\": \"<Concise summary of past symptoms and complaints>\",\n");
        sb.append("  \"diagnosisHistory\": \"<Chronological or categorized summary of diagnostic history>\",\n");
        sb.append("  \"treatmentsSummary\": \"<Summary of past and ongoing treatments and prescribed medications>\",\n");
        sb.append("  \"followUpDirectives\": \"<Summary of upcoming follow-ups and directives>\",\n");
        sb.append("  \"rawExecutiveSummary\": \"<High-level clinical executive summary synthesizing the patient medical trajectory>\"\n");
        sb.append("}\n");

        return sb.toString();
    }

    private AiSummaryDTO parseAndBuildSummary(Patient patient, List<Consultation> consultations, String rawResponse) throws Exception {
        String cleanJson = rawResponse != null ? rawResponse.trim() : "";
        if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.substring(7);
        } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.substring(3);
        }
        if (cleanJson.endsWith("```")) {
            cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
        }
        cleanJson = cleanJson.trim();

        JsonNode node = objectMapper.readTree(cleanJson);
        String chiefComplaints = node.path("chiefComplaintsSummary").asText("");
        String diagnosisHistory = node.path("diagnosisHistory").asText("");
        String treatmentsSummary = node.path("treatmentsSummary").asText("");
        String followUpDirectives = node.path("followUpDirectives").asText("");
        String rawExecutiveSummary = node.path("rawExecutiveSummary").asText("");

        return AiSummaryDTO.builder()
                .patientId(patient.getId())
                .patientName(patient.getFullName())
                .totalConsultationsAnalyzed(consultations.size())
                .chiefComplaintsSummary(chiefComplaints.isBlank() ? "No complaints recorded." : chiefComplaints)
                .diagnosisHistory(diagnosisHistory.isBlank() ? "No diagnostic history recorded." : diagnosisHistory)
                .treatmentsSummary(treatmentsSummary.isBlank() ? "No treatments recorded." : treatmentsSummary)
                .followUpDirectives(followUpDirectives.isBlank() ? "No specific follow-up directives." : followUpDirectives)
                .rawExecutiveSummary(rawExecutiveSummary)
                .isAiGenerated(true)
                .build();
    }

    private AiSummaryDTO buildLocalFallbackSummary(Patient patient, List<Consultation> consultations) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        String complaintsSummary = consultations.stream()
                .map(c -> "• " + c.getConsultationDate().format(dtf) + ": " + c.getSymptoms())
                .collect(Collectors.joining("\n"));

        String diagnosisHistory = consultations.stream()
                .map(c -> "• " + c.getConsultationDate().format(dtf) + (c.getDoctor() != null ? " (Dr. " + c.getDoctor().getLastName() + ")" : "") + ": " + c.getDiagnosis())
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
                .isAiGenerated(false)
                .build();
    }
}
