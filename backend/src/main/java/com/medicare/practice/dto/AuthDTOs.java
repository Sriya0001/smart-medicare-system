package com.medicare.practice.dto;

import com.medicare.practice.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

public class AuthDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegisterRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        @NotNull(message = "Role is required")
        private Role role; // ROLE_PATIENT or ROLE_DOCTOR

        // Patient specific optional fields
        private LocalDate dateOfBirth;
        private String gender;
        private String phoneNumber;
        private String address;
        private String bloodGroup;
        private String emergencyContact;

        // Doctor specific optional fields
        private String specialty;
        private String licenseNumber;
        private Integer experienceYears;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthResponse {
        private String token;
        private String type = "Bearer";
        private Long userId;
        private String email;
        private Role role;
        private Long profileId; // Patient ID or Doctor ID
        private String fullName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserDTO {
        private Long id;
        private String email;
        private Role role;
        private boolean enabled;
    }
}
