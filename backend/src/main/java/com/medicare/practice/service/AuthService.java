package com.medicare.practice.service;

import com.medicare.practice.dto.AuthDTOs.*;
import com.medicare.practice.entity.*;
import com.medicare.practice.exception.BadRequestException;
import com.medicare.practice.exception.ConflictException;
import com.medicare.practice.exception.ResourceNotFoundException;
import com.medicare.practice.repository.DoctorAvailabilityRepository;
import com.medicare.practice.repository.DoctorRepository;
import com.medicare.practice.repository.PatientRepository;
import com.medicare.practice.repository.UserRepository;
import com.medicare.practice.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String jwt = jwtUtils.generateToken(authentication);

        Long profileId = null;
        String fullName = "System Admin";

        if (user.getRole() == Role.ROLE_PATIENT) {
            Patient patient = patientRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));
            profileId = patient.getId();
            fullName = patient.getFullName();
        } else if (user.getRole() == Role.ROLE_DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found"));
            profileId = doctor.getId();
            fullName = doctor.getFullName();
        }

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .profileId(profileId)
                .fullName(fullName)
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("An account with email " + normalizedEmail + " already exists.");
        }

        if (request.getRole() == Role.ROLE_ADMIN) {
            throw new BadRequestException("Admin accounts cannot be registered via public registration.");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Long profileId;
        String fullName;

        if (request.getRole() == Role.ROLE_PATIENT) {
            Patient patient = Patient.builder()
                    .user(user)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .dateOfBirth(request.getDateOfBirth())
                    .gender(request.getGender())
                    .phoneNumber(request.getPhoneNumber())
                    .address(request.getAddress())
                    .bloodGroup(request.getBloodGroup())
                    .emergencyContact(request.getEmergencyContact())
                    .build();
            patient = patientRepository.save(patient);
            profileId = patient.getId();
            fullName = patient.getFullName();
        } else {
            Doctor doctor = Doctor.builder()
                    .user(user)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .specialty(request.getSpecialty() != null ? request.getSpecialty() : "General Practice")
                    .licenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "LIC-" + System.currentTimeMillis())
                    .phoneNumber(request.getPhoneNumber())
                    .experienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 5)
                    .active(true)
                    .build();
            doctor = doctorRepository.save(doctor);
            profileId = doctor.getId();
            fullName = doctor.getFullName();

            // Setup default Monday-Friday 09:00-17:00 availability
            for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
                DoctorAvailability da = DoctorAvailability.builder()
                        .doctor(doctor)
                        .dayOfWeek(day)
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .slotDurationMinutes(30)
                        .active(true)
                        .build();
                availabilityRepository.save(da);
            }
        }

        String jwt = jwtUtils.generateTokenFromEmailAndRole(user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .profileId(profileId)
                .fullName(fullName)
                .build();
    }
}
