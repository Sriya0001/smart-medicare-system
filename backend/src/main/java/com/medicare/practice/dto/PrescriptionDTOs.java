package com.medicare.practice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PrescriptionDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrescriptionDTO {
        private Long id;
        private Long consultationId;
        private Long patientId;
        private String patientName;
        private Long doctorId;
        private String doctorName;
        private LocalDate issueDate;
        private String notes;
        private List<PrescriptionItemDTO> items;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrescriptionItemDTO {
        private Long id;
        private String medicationName;
        private String dosage;
        private String frequency;
        private String duration;
        private String instructions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrescriptionItemRequest {
        @NotBlank(message = "Medication name is required")
        private String medicationName;

        @NotBlank(message = "Dosage is required")
        private String dosage;

        @NotBlank(message = "Frequency is required")
        private String frequency;

        @NotBlank(message = "Duration is required")
        private String duration;

        private String instructions;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PrescriptionCreateRequest {
        private Long consultationId;

        @NotNull(message = "Patient ID is required")
        private Long patientId;

        @NotEmpty(message = "At least one medication item is required")
        private List<PrescriptionItemRequest> items;

        private String notes;
    }
}
