package com.medicare.practice.controller;

import com.medicare.practice.dto.AppointmentDTOs.*;
import com.medicare.practice.dto.DoctorAvailabilityDTOs.AvailableSlotDTO;
import com.medicare.practice.service.AppointmentService;
import com.medicare.practice.service.AvailabilityService;
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
@RequestMapping("/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Endpoints for appointment booking, availability, status transitions, and cancellation")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all practice appointments (Admin only)")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get appointment details by ID")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @GetMapping("/available-slots")
    @Operation(summary = "Get available discrete time slots for a doctor on a specific date")
    public ResponseEntity<List<AvailableSlotDTO>> getAvailableSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.calculateAvailableSlots(doctorId, date));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Book a new appointment with double-booking prevention")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Update appointment status (CONFIRMED, COMPLETED, CANCELLED)")
    public ResponseEntity<AppointmentDTO> updateAppointmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Cancel an appointment")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, reason));
    }
}
