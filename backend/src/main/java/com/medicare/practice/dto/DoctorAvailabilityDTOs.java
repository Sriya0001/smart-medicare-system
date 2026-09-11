package com.medicare.practice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorAvailabilityDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorAvailabilityDTO {
        private Long id;
        private Long doctorId;
        private String doctorName;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer slotDurationMinutes;
        private boolean active;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorAvailabilityRequest {
        @NotNull(message = "Day of week is required")
        private DayOfWeek dayOfWeek;

        @NotNull(message = "Start time is required")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        private LocalTime endTime;

        @Builder.Default
        private Integer slotDurationMinutes = 30;

        @Builder.Default
        private Boolean active = true;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AvailableSlotDTO {
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private boolean available;
        private String reason; // e.g. "Available", "Booked", "Past time"
    }
}
