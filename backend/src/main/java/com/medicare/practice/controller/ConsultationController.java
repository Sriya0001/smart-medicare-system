package com.medicare.practice.controller;

import com.medicare.practice.dto.ConsultationDTOs.*;
import com.medicare.practice.service.AiSummarizerService;
import com.medicare.practice.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/consultations")
@RequiredArgsConstructor
@Tag(name = "Consultations", description = "Endpoints for consultation records, SOAP notes, and patient history summaries")
public class ConsultationController {

    private final ConsultationService consultationService;
    private final AiSummarizerService aiSummarizerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all consultations across the practice (Admin only)")
    public ResponseEntity<List<ConsultationDTO>> getAllConsultations() {
        return ResponseEntity.ok(consultationService.getAllConsultations());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get consultation details by ID")
    public ResponseEntity<ConsultationDTO> getConsultationById(@PathVariable Long id) {
        return ResponseEntity.ok(consultationService.getConsultationById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    @Operation(summary = "Get consultation by Appointment ID")
    public ResponseEntity<ConsultationDTO> getConsultationByAppointmentId(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(consultationService.getConsultationByAppointmentId(appointmentId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Create consultation notes and auto-complete appointment")
    public ResponseEntity<ConsultationDTO> createConsultation(@Valid @RequestBody ConsultationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.createConsultation(request));
    }

    @GetMapping("/patient/{patientId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Summarize patient consultation history (AI documentation support feature)")
    public ResponseEntity<AiSummaryDTO> getPatientHistorySummary(@PathVariable Long patientId) {
        return ResponseEntity.ok(aiSummarizerService.summarizePatientHistory(patientId));
    }
}
