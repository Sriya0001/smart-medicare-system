package com.medicare.practice.controller;

import com.medicare.practice.dto.AppointmentDTOs.AppointmentDTO;
import com.medicare.practice.dto.ConsultationDTOs.ConsultationDTO;
import com.medicare.practice.dto.PatientDTOs.*;
import com.medicare.practice.dto.PrescriptionDTOs.PrescriptionDTO;
import com.medicare.practice.service.AppointmentService;
import com.medicare.practice.service.ConsultationService;
import com.medicare.practice.service.PatientService;
import com.medicare.practice.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
@Tag(name = "Patients", description = "Endpoints for managing patient profiles, appointments, and medical history")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final PrescriptionService prescriptionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all patients (Admin only)")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Search patients by name, email, or phone")
    public ResponseEntity<List<PatientDTO>> searchPatients(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(patientService.searchPatients(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get patient profile by ID")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get patient profile by User ID")
    public ResponseEntity<PatientDTO> getPatientByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(patientService.getPatientByUserId(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Update patient profile information")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientProfileRequest request) {
        return ResponseEntity.ok(patientService.updatePatientProfile(id, request));
    }

    @GetMapping("/{id}/appointments")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get all appointments for a patient")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointments(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatientId(id));
    }

    @GetMapping("/{id}/consultations")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get consultation/medical record history for a patient")
    public ResponseEntity<List<ConsultationDTO>> getPatientConsultations(@PathVariable Long id) {
        return ResponseEntity.ok(consultationService.getConsultationsByPatientId(id));
    }

    @GetMapping("/{id}/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get prescriptions history for a patient")
    public ResponseEntity<List<PrescriptionDTO>> getPatientPrescriptions(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByPatientId(id));
    }
}
