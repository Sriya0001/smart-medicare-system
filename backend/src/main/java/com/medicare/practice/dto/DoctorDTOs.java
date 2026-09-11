package com.medicare.practice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

public class DoctorDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorDTO {
        private Long id;
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private String specialty;
        private String licenseNumber;
        private String phoneNumber;
        private Integer experienceYears;
        private String biography;
        private BigDecimal consultationFee;
        private boolean active;
        private List<DoctorAvailabilityDTOs.DoctorAvailabilityDTO> availabilities;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorCreateRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Specialty is required")
        private String specialty;

        @NotBlank(message = "License number is required")
        private String licenseNumber;

        private String phoneNumber;
        private Integer experienceYears;
        private String biography;
        private BigDecimal consultationFee;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorUpdateRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotBlank(message = "Specialty is required")
        private String specialty;

        private String licenseNumber;
        private String phoneNumber;
        private Integer experienceYears;
        private String biography;
        private BigDecimal consultationFee;
        private Boolean active;
    }
}
