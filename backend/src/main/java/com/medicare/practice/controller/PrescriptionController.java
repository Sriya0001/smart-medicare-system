package com.medicare.practice.controller;

import com.medicare.practice.dto.PrescriptionDTOs.*;
import com.medicare.practice.security.UserPrincipal;
import com.medicare.practice.service.DoctorService;
import com.medicare.practice.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescriptions", description = "Endpoints for electronic prescriptions and medication items")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all prescriptions (Admin only)")
    public ResponseEntity<List<PrescriptionDTO>> getAllPrescriptions() {
        return ResponseEntity.ok(prescriptionService.getAllPrescriptions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get prescription details with medication items by ID")
    public ResponseEntity<PrescriptionDTO> getPrescriptionById(@PathVariable Long id) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Create an electronic prescription")
    public ResponseEntity<PrescriptionDTO> createPrescription(
            @RequestParam(required = false) Long doctorId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PrescriptionCreateRequest request) {

        Long effectiveDoctorId = doctorId;
        if (effectiveDoctorId == null && principal != null) {
            effectiveDoctorId = doctorService.getDoctorByUserId(principal.getId()).getId();
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(prescriptionService.createPrescription(effectiveDoctorId, request));
    }
}
