package com.medicare.practice.dto;

import com.medicare.practice.entity.AppointmentStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppointmentDTO {
        private Long id;
        private Long patientId;
        private String patientName;
        private String patientPhone;
        private String patientEmail;
        private Long doctorId;
        private String doctorName;
        private String doctorSpecialty;
        private LocalDate appointmentDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private AppointmentStatus status;
        private String reason;
        private String notes;
        private String cancellationReason;
        private Long consultationId;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppointmentCreateRequest {
        @NotNull(message = "Doctor ID is required")
        private Long doctorId;

        // If patient books, patientId inferred from auth; if admin books, admin specifies patientId
        private Long patientId;

        @NotNull(message = "Appointment date is required")
        @FutureOrPresent(message = "Appointment date cannot be in the past")
        private LocalDate appointmentDate;

        @NotNull(message = "Start time is required")
        private LocalTime startTime;

        private LocalTime endTime; // optional, defaults to startTime + slotDuration

        @NotBlank(message = "Reason for appointment is required")
        private String reason;

        private String notes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppointmentStatusUpdateRequest {
        @NotNull(message = "Status is required")
        private AppointmentStatus status;

        private String cancellationReason;
        private String notes;
    }
}
