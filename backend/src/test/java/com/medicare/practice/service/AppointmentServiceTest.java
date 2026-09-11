package com.medicare.practice.service;

import com.medicare.practice.dto.AppointmentDTOs.*;
import com.medicare.practice.dto.DoctorAvailabilityDTOs.AvailableSlotDTO;
import com.medicare.practice.entity.*;
import com.medicare.practice.exception.BadRequestException;
import com.medicare.practice.exception.ConflictException;
import com.medicare.practice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class AppointmentServiceTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    private Doctor doctor;
    private Patient patient1;
    private Patient patient2;
    private LocalDate targetDate;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        doctorRepository.deleteAll();
        patientRepository.deleteAll();
        userRepository.deleteAll();

        // Find next Monday
        targetDate = LocalDate.now().plusDays(1);
        while (targetDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            targetDate = targetDate.plusDays(1);
        }

        User docUser = userRepository.save(User.builder()
                .email("dr.smith@test.com")
                .password("hash")
                .role(Role.ROLE_DOCTOR)
                .enabled(true)
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .user(docUser)
                .firstName("John")
                .lastName("Smith")
                .specialty("Cardiology")
                .licenseNumber("MD-12345")
                .consultationFee(new BigDecimal("150.00"))
                .active(true)
                .build());

        // Working hours Monday 09:00 - 12:00 (30-min slots = 6 slots: 09:00, 09:30, 10:00, 10:30, 11:00, 11:30)
        availabilityRepository.save(DoctorAvailability.builder()
                .doctor(doctor)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(12, 0))
                .slotDurationMinutes(30)
                .active(true)
                .build());

        User patUser1 = userRepository.save(User.builder()
                .email("alice@test.com")
                .password("hash")
                .role(Role.ROLE_PATIENT)
                .enabled(true)
                .build());

        patient1 = patientRepository.save(Patient.builder()
                .user(patUser1)
                .firstName("Alice")
                .lastName("Walker")
                .build());

        User patUser2 = userRepository.save(User.builder()
                .email("bob@test.com")
                .password("hash")
                .role(Role.ROLE_PATIENT)
                .enabled(true)
                .build());

        patient2 = patientRepository.save(Patient.builder()
                .user(patUser2)
                .firstName("Bob")
                .lastName("Taylor")
                .build());
    }

    @Test
    @DisplayName("Should correctly calculate all 6 available slots for Monday morning")
    void testCalculateAvailableSlots_Success() {
        List<AvailableSlotDTO> slots = availabilityService.calculateAvailableSlots(doctor.getId(), targetDate);

        assertNotNull(slots);
        assertEquals(6, slots.size(), "Should have exactly 6 thirty-minute slots between 09:00 and 12:00");
        assertTrue(slots.stream().allMatch(AvailableSlotDTO::isAvailable));
        assertEquals(LocalTime.of(9, 0), slots.get(0).getStartTime());
        assertEquals(LocalTime.of(9, 30), slots.get(0).getEndTime());
        assertEquals(LocalTime.of(11, 30), slots.get(5).getStartTime());
        assertEquals(LocalTime.of(12, 0), slots.get(5).getEndTime());
    }

    @Test
    @DisplayName("Should successfully book an appointment within available hours")
    void testCreateAppointment_Success() {
        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(doctor.getId())
                .patientId(patient1.getId())
                .appointmentDate(targetDate)
                .startTime(LocalTime.of(9, 30))
                .reason("Routine heart checkup")
                .build();

        AppointmentDTO booked = appointmentService.createAppointment(request);

        assertNotNull(booked);
        assertNotNull(booked.getId());
        assertEquals(AppointmentStatus.BOOKED, booked.getStatus());
        assertEquals(LocalTime.of(9, 30), booked.getStartTime());
        assertEquals(LocalTime.of(10, 0), booked.getEndTime());

        // Now verify available slots reflects that 09:30 is Booked
        List<AvailableSlotDTO> slots = availabilityService.calculateAvailableSlots(doctor.getId(), targetDate);
        AvailableSlotDTO bookedSlot = slots.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(9, 30)))
                .findFirst()
                .orElse(null);

        assertNotNull(bookedSlot);
        assertFalse(bookedSlot.isAvailable());
        assertEquals("Booked", bookedSlot.getReason());
    }

    @Test
    @DisplayName("Should prevent double-booking when second patient attempts to book the same slot")
    void testDoubleBooking_ThrowsConflictException() {
        AppointmentCreateRequest request1 = AppointmentCreateRequest.builder()
                .doctorId(doctor.getId())
                .patientId(patient1.getId())
                .appointmentDate(targetDate)
                .startTime(LocalTime.of(10, 0))
                .reason("First booking")
                .build();
        appointmentService.createAppointment(request1);

        AppointmentCreateRequest request2 = AppointmentCreateRequest.builder()
                .doctorId(doctor.getId())
                .patientId(patient2.getId())
                .appointmentDate(targetDate)
                .startTime(LocalTime.of(10, 0))
                .reason("Second concurrent booking attempt")
                .build();

        ConflictException ex = assertThrows(ConflictException.class, () -> appointmentService.createAppointment(request2));
        assertTrue(ex.getMessage().contains("is no longer available"));
    }

    @Test
    @DisplayName("Should reject booking outside doctor's configured working hours")
    void testBookingOutsideWorkingHours_ThrowsBadRequestException() {
        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(doctor.getId())
                .patientId(patient1.getId())
                .appointmentDate(targetDate)
                .startTime(LocalTime.of(15, 0)) // Monday only has 09:00 - 12:00
                .reason("Afternoon request outside schedule")
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(request));
        assertTrue(ex.getMessage().contains("outside the doctor's working hours"));
    }

    @Test
    @DisplayName("Should reject booking in the past")
    void testBookingInPast_ThrowsBadRequestException() {
        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(doctor.getId())
                .patientId(patient1.getId())
                .appointmentDate(LocalDate.now().minusDays(1))
                .startTime(LocalTime.of(10, 0))
                .reason("Past appointment")
                .build();

        BadRequestException ex = assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(request));
        assertTrue(ex.getMessage().contains("in the past"));
    }

    @Test
    @DisplayName("Should free up the slot when an appointment is cancelled")
    void testCancelAppointment_FreesSlot() {
        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(doctor.getId())
                .patientId(patient1.getId())
                .appointmentDate(targetDate)
                .startTime(LocalTime.of(11, 0))
                .reason("Pre-cancellation test")
                .build();
        AppointmentDTO booked = appointmentService.createAppointment(request);

        // Cancel it
        appointmentService.cancelAppointment(booked.getId(), "Patient schedule change");

        // Verify slot is available again
        List<AvailableSlotDTO> slots = availabilityService.calculateAvailableSlots(doctor.getId(), targetDate);
        AvailableSlotDTO slot = slots.stream()
                .filter(s -> s.getStartTime().equals(LocalTime.of(11, 0)))
                .findFirst()
                .orElse(null);

        assertNotNull(slot);
        assertTrue(slot.isAvailable(), "Slot should become available after cancellation");
    }
}
