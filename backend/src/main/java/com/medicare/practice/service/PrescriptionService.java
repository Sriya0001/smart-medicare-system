package com.medicare.practice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicare.practice.dto.PrescriptionDTOs.*;
import com.medicare.practice.entity.*;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.ConsultationRepository;
import com.medicare.practice.repository.DoctorRepository;
import com.medicare.practice.repository.PatientRepository;
import com.medicare.practice.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final ConsultationRepository consultationRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

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

    @Transactional(readOnly = true)
    public PrescriptionExplanationDTO explainPrescription(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        if (geminiClient.isEnabled()) {
            try {
                String prompt = buildExplanationPrompt(prescription);
                String rawResponse = geminiClient.generateContent(prompt);
                return parseAndBuildExplanation(prescription, rawResponse);
            } catch (Exception e) {
                log.warn("Gemini plain-language prescription explainer failed, falling back to local guidance. Reason: {}", e.getMessage());
            }
        } else {
            log.info("Gemini API is not enabled/configured. Using local prescription explanation fallback.");
        }

        return buildFallbackExplanation(prescription);
    }

    private String buildExplanationPrompt(Prescription p) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a friendly clinical pharmacist explaining medical prescriptions in plain, clear, everyday language (approx. 7th-8th grade reading level) for a patient.\n\n");
        sb.append("PRESCRIPTION DETAILS:\n");
        if (p.getNotes() != null && !p.getNotes().isBlank()) {
            sb.append("Doctor's Directives/Notes: ").append(p.getNotes()).append("\n");
        }
        if (p.getConsultation() != null && p.getConsultation().getDiagnosis() != null) {
            sb.append("Associated Clinical Diagnosis: ").append(p.getConsultation().getDiagnosis()).append("\n");
        }
        sb.append("PRESCRIBED MEDICATIONS:\n");
        for (PrescriptionItem item : p.getItems()) {
            sb.append(String.format("- %s (Dosage: %s, Frequency: %s, Duration: %s, Instructions: %s)\n",
                    item.getMedicationName(),
                    item.getDosage() != null ? item.getDosage() : "As directed",
                    item.getFrequency() != null ? item.getFrequency() : "As directed",
                    item.getDuration() != null ? item.getDuration() : "As prescribed",
                    item.getInstructions() != null ? item.getInstructions() : "None"
            ));
        }

        sb.append("\nINSTRUCTIONS:\n");
        sb.append("Return ONLY a valid JSON object matching this exact structure without markdown code blocks:\n");
        sb.append("{\n");
        sb.append("  \"overview\": \"<A warm, reassuring 1-2 sentence overview explaining the goal of this regimen>\",\n");
        sb.append("  \"medications\": [\n");
        sb.append("    {\n");
        sb.append("      \"medicationName\": \"<Medication name>\",\n");
        sb.append("      \"purpose\": \"<Plain-English explanation of why the patient needs this and how it helps>\",\n");
        sb.append("      \"howToTake\": \"<Clear everyday guidance on timing, with/without meals, and duration>\",\n");
        sb.append("      \"commonSideEffects\": \"<Common mild side effects to expect and how to handle them>\"\n");
        sb.append("    }\n");
        sb.append("  ],\n");
        sb.append("  \"dietaryAndLifestyleAdvice\": \"<Foods, drinks, or habits to avoid or adopt during treatment>\",\n");
        sb.append("  \"whenToCallDoctor\": \"<Warning symptoms that require prompt medical attention>\"\n");
        sb.append("}\n");

        return sb.toString();
    }

    private PrescriptionExplanationDTO parseAndBuildExplanation(Prescription p, String rawResponse) throws Exception {
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

        JsonNode root = objectMapper.readTree(cleanJson);
        String overview = root.path("overview").asText("");
        String dietary = root.path("dietaryAndLifestyleAdvice").asText("");
        String whenToCall = root.path("whenToCallDoctor").asText("");

        List<MedicationPlainExplanationDTO> meds = new ArrayList<>();
        JsonNode medsArray = root.path("medications");
        if (medsArray.isArray()) {
            for (JsonNode m : medsArray) {
                meds.add(MedicationPlainExplanationDTO.builder()
                        .medicationName(m.path("medicationName").asText())
                        .purpose(m.path("purpose").asText())
                        .howToTake(m.path("howToTake").asText())
                        .commonSideEffects(m.path("commonSideEffects").asText())
                        .build());
            }
        }

        if (meds.isEmpty()) {
            return buildFallbackExplanation(p);
        }

        return PrescriptionExplanationDTO.builder()
                .prescriptionId(p.getId())
                .overview(overview.isBlank() ? "Your doctor has prescribed this regimen to support your recovery." : overview)
                .medications(meds)
                .dietaryAndLifestyleAdvice(dietary.isBlank() ? "Stay hydrated and take your medications at regular intervals." : dietary)
                .whenToCallDoctor(whenToCall.isBlank() ? "Contact your doctor if symptoms worsen or you experience adverse reactions." : whenToCall)
                .isAiGenerated(true)
                .build();
    }

    private PrescriptionExplanationDTO buildFallbackExplanation(Prescription p) {
        List<MedicationPlainExplanationDTO> meds = p.getItems().stream()
                .map(item -> MedicationPlainExplanationDTO.builder()
                        .medicationName(item.getMedicationName())
                        .purpose("Prescribed by Dr. " + (p.getDoctor() != null ? p.getDoctor().getLastName() : "your physician") + " to treat your diagnosed condition.")
                        .howToTake("Take " + item.getDosage() + " (" + item.getFrequency() + ") for " + item.getDuration() + ". " +
                                (item.getInstructions() != null ? item.getInstructions() : "Follow physician instructions."))
                        .commonSideEffects("Contact your doctor if you experience unexpected or uncomfortable symptoms.")
                        .build())
                .collect(Collectors.toList());

        return PrescriptionExplanationDTO.builder()
                .prescriptionId(p.getId())
                .overview("This prescription was issued by Dr. " + (p.getDoctor() != null ? p.getDoctor().getFullName() : "your doctor") + ". Please follow all dosage instructions closely.")
                .medications(meds)
                .dietaryAndLifestyleAdvice("Stay well hydrated and take your medications at consistent times each day.")
                .whenToCallDoctor("Contact the practice if your symptoms persist or if you experience severe allergic reactions.")
                .isAiGenerated(false)
                .build();
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
