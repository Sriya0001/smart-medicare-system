package com.medicare.practice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ConsultationDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsultationDTO {
        private Long id;
        private Long appointmentId;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private String doctorSpecialty;
        private LocalDateTime consultationDate;
        private String symptoms;
        private String diagnosis;
        private String treatmentPlan;
        private String notes;
        private LocalDate followUpDate;
        private List<PrescriptionDTOs.PrescriptionDTO> prescriptions;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ConsultationCreateRequest {
        @NotNull(message = "Appointment ID is required")
        private Long appointmentId;

        @NotBlank(message = "Symptoms / Chief Complaint is required")
        private String symptoms;

        @NotBlank(message = "Diagnosis is required")
        private String diagnosis;

        private String treatmentPlan;
        private String notes;
        private LocalDate followUpDate;

        @Valid
        private List<PrescriptionDTOs.PrescriptionItemRequest> prescriptionItems;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AiSummaryDTO {
        private Long patientId;
        private String patientName;
        private int totalConsultationsAnalyzed;
        private String chiefComplaintsSummary;
        private String diagnosisHistory;
        private String treatmentsSummary;
        private String followUpDirectives;
        private String rawExecutiveSummary;
        private boolean isAiGenerated;
    }
}
