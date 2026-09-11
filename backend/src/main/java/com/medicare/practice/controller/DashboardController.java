package com.medicare.practice.controller;

import com.medicare.practice.dto.DashboardDTOs.*;
import com.medicare.practice.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints for aggregated KPIs, schedules, and metrics by role")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin dashboard overview KPIs, appointment counts, and status breakdown")
    public ResponseEntity<AdminDashboardDTO> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboard());
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    @Operation(summary = "Get doctor dashboard today agenda and upcoming appointments")
    public ResponseEntity<DoctorDashboardDTO> getDoctorDashboard(@PathVariable Long doctorId) {
        return ResponseEntity.ok(dashboardService.getDoctorDashboard(doctorId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    @Operation(summary = "Get patient dashboard next upcoming appointment and active prescriptions")
    public ResponseEntity<PatientDashboardDTO> getPatientDashboard(@PathVariable Long patientId) {
        return ResponseEntity.ok(dashboardService.getPatientDashboard(patientId));
    }
}
