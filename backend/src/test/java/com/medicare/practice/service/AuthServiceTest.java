package com.medicare.practice.service;

import com.medicare.practice.dto.AuthDTOs.*;
import com.medicare.practice.entity.Role;
import com.medicare.practice.exception.ConflictException;
import com.medicare.practice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @BeforeEach
    void setUp() {
        prescriptionRepository.deleteAll();
        consultationRepository.deleteAll();
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully register a patient and generate JWT")
    void testRegisterPatient_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("clara.oswald@test.com")
                .password("SecurePass123!")
                .firstName("Clara")
                .lastName("Oswald")
                .role(Role.ROLE_PATIENT)
                .dateOfBirth(LocalDate.of(1995, 3, 15))
                .gender("Female")
                .phoneNumber("+1 555-123-9999")
                .bloodGroup("O+")
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("clara.oswald@test.com", response.getEmail());
        assertEquals(Role.ROLE_PATIENT, response.getRole());
        assertNotNull(response.getProfileId());
        assertEquals("Clara Oswald", response.getFullName());
    }

    @Test
    @DisplayName("Should throw ConflictException when registering duplicate email")
    void testRegisterDuplicateEmail_ThrowsConflictException() {
        RegisterRequest request1 = RegisterRequest.builder()
                .email("duplicate@test.com")
                .password("Password123")
                .firstName("First")
                .lastName("User")
                .role(Role.ROLE_PATIENT)
                .build();
        authService.register(request1);

        RegisterRequest request2 = RegisterRequest.builder()
                .email("duplicate@test.com")
                .password("AnotherPassword")
                .firstName("Second")
                .lastName("User")
                .role(Role.ROLE_PATIENT)
                .build();

        assertThrows(ConflictException.class, () -> authService.register(request2));
    }

    @Test
    @DisplayName("Should successfully login and return JWT for existing user")
    void testLogin_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("doctor.who@test.com")
                .password("Tardis2026!")
                .firstName("Doctor")
                .lastName("Who")
                .role(Role.ROLE_DOCTOR)
                .specialty("General Practice")
                .licenseNumber("TIME-LORD-1")
                .build();
        authService.register(request);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("doctor.who@test.com")
                .password("Tardis2026!")
                .build();

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("doctor.who@test.com", response.getEmail());
        assertEquals(Role.ROLE_DOCTOR, response.getRole());
    }
}
