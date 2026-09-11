package com.medicare.practice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatientDTO {
        private Long id;
        private Long userId;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private LocalDate dateOfBirth;
        private String gender;
        private String phoneNumber;
        private String address;
        private String bloodGroup;
        private String emergencyContact;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatientProfileRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private LocalDate dateOfBirth;
        private String gender;
        private String phoneNumber;
        private String address;
        private String bloodGroup;
        private String emergencyContact;
    }
}
