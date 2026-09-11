package com.medicare.practice.controller;

import com.medicare.practice.dto.AppointmentDTOs.AppointmentDTO;
import com.medicare.practice.dto.DoctorAvailabilityDTOs.*;
import com.medicare.practice.dto.DoctorDTOs.*;
import com.medicare.practice.dto.PatientDTOs.PatientDTO;
import com.medicare.practice.service.AppointmentService;
import com.medicare.practice.service.AvailabilityService;
import com.medicare.practice.service.DoctorService;
import com.medicare.practice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctors", description = "Endpoints for doctor directory, scheduling, and profile management")
public class DoctorController {

    private final DoctorService doctorService;
    private final AvailabilityService availabilityService;
    private final AppointmentService appointmentService;
    private final PatientService patientService;

    @GetMapping
    @Operation(summary = "Get all active doctors (Public)")
    public ResponseEntity<List<DoctorDTO>> getAllDoctors(@RequestParam(required = false) String specialty) {
        if (specialty != null && !specialty.isBlank()) {
            return ResponseEntity.ok(doctorService.getDoctorsBySpecialty(specialty));
        }
        return ResponseEntity.ok(doctorService.getAllActiveDoctors());
    }

    @GetMapping("/admin-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all doctors including inactive (Admin only)")
    public ResponseEntity<List<DoctorDTO>> getAllDoctorsForAdmin() {
        return ResponseEntity.ok(doctorService.getAllDoctorsForAdmin());
    }

    @GetMapping("/specialties")
    @Operation(summary = "Get list of all active doctor specialties (Public)")
    public ResponseEntity<List<String>> getAllSpecialties() {
        return ResponseEntity.ok(doctorService.getAllSpecialties());
    }

    @GetMapping("/search")
    @Operation(summary = "Search doctors by name or specialty (Public)")
    public ResponseEntity<List<DoctorDTO>> searchDoctors(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(doctorService.searchDoctors(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor details by ID (Public)")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get doctor profile by User ID")
    public ResponseEntity<DoctorDTO> getDoctorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(doctorService.getDoctorByUserId(userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new doctor (Admin only)")
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.createDoctor(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Update doctor details")
    public ResponseEntity<DoctorDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorUpdateRequest request) {
        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate doctor (Admin only)")
    public ResponseEntity<Void> deactivateDoctor(@PathVariable Long id) {
        doctorService.deactivateDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get doctor's weekly recurring availability schedule (Public)")
    public ResponseEntity<List<DoctorAvailabilityDTO>> getDoctorAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(availabilityService.getDoctorAvailabilities(id));
    }

    @PostMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Set or update doctor's availability schedule for a day")
    public ResponseEntity<DoctorAvailabilityDTO> setDoctorAvailability(
            @PathVariable Long id,
            @Valid @RequestBody DoctorAvailabilityRequest request) {
        return ResponseEntity.ok(availabilityService.setAvailability(id, request));
    }

    @GetMapping("/{id}/available-slots")
    @Operation(summary = "Calculate real-time discrete available appointment slots for a specific date (Public)")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.calculateAvailableSlots(id, date));
    }

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get all appointments for a doctor")
    public ResponseEntity<List<AppointmentDTO>> getDoctorAppointments(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorId(id));
    }

    @GetMapping("/{id}/today-appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get today's agenda for a doctor")
    public ResponseEntity<List<AppointmentDTO>> getTodayAppointments(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getTodayDoctorAppointments(id));
    }

    @GetMapping("/{id}/patients")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get patients who have booked appointments with this doctor")
    public ResponseEntity<List<PatientDTO>> getDoctorPatients(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientsByDoctorId(id));
    }
}
