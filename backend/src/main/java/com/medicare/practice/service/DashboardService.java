package com.medicare.practice.service;

import com.medicare.practice.dto.AppointmentDTOs.AppointmentDTO;
import com.medicare.practice.dto.ConsultationDTOs.ConsultationDTO;
import com.medicare.practice.dto.DashboardDTOs.*;
import com.medicare.practice.dto.PrescriptionDTOs.PrescriptionDTO;
import com.medicare.practice.entity.AppointmentStatus;
import com.medicare.practice.entity.Role;
import com.medicare.practice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentService appointmentService;
    private final ConsultationService consultationService;
    private final PrescriptionService prescriptionService;

    @Transactional(readOnly = true)
    public AdminDashboardDTO getAdminDashboard() {
        long totalDoctors = doctorRepository.countByActiveTrue();
        long totalPatients = patientRepository.count();
        long totalAppointments = appointmentRepository.count();
        long todayAppointments = appointmentRepository.countByAppointmentDate(LocalDate.now());
        long pendingBookings = appointmentRepository.countByStatus(AppointmentStatus.BOOKED);

        Map<AppointmentStatus, Long> breakdown = new HashMap<>();
        for (AppointmentStatus status : AppointmentStatus.values()) {
            breakdown.put(status, appointmentRepository.countByStatus(status));
        }

        List<AppointmentDTO> recent = appointmentRepository.findAll().stream()
                .sorted((a, b) -> b.getAppointmentDate().compareTo(a.getAppointmentDate()))
                .limit(5)
                .map(appointmentService::mapToDTO)
                .collect(Collectors.toList());

        return AdminDashboardDTO.builder()
                .totalDoctors(totalDoctors)
                .totalPatients(totalPatients)
                .totalAppointments(totalAppointments)
                .todayAppointmentsCount(todayAppointments)
                .pendingBookingsCount(pendingBookings)
                .statusBreakdown(breakdown)
                .recentAppointments(recent)
                .build();
    }

    @Transactional(readOnly = true)
    public DoctorDashboardDTO getDoctorDashboard(Long doctorId) {
        long assignedPatients = patientRepository.findPatientsByDoctorId(doctorId).size();
        long todayCount = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, LocalDate.now()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .count();
        long upcomingCount = appointmentRepository.findUpcomingDoctorAppointments(doctorId, LocalDate.now()).stream()
                .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                .count();
        long completedCount = appointmentRepository.countByDoctorIdAndStatus(doctorId, AppointmentStatus.COMPLETED);

        List<AppointmentDTO> todayAppointments = appointmentService.getTodayDoctorAppointments(doctorId);
        List<AppointmentDTO> upcomingAppointments = appointmentService.getUpcomingDoctorAppointments(doctorId).stream()
                .limit(5)
                .collect(Collectors.toList());

        return DoctorDashboardDTO.builder()
                .totalAssignedPatients(assignedPatients)
                .todayAppointmentsCount(todayCount)
                .upcomingAppointmentsCount(upcomingCount)
                .completedConsultationsCount(completedCount)
                .todayAppointments(todayAppointments)
                .upcomingAppointments(upcomingAppointments)
                .build();
    }

    @Transactional(readOnly = true)
    public PatientDashboardDTO getPatientDashboard(Long patientId) {
        long totalAppointments = appointmentRepository.countByPatientId(patientId);
        List<AppointmentDTO> upcoming = appointmentService.getUpcomingPatientAppointments(patientId);
        long upcomingCount = upcoming.size();
        long totalPrescriptions = prescriptionRepository.countByPatientId(patientId);
        long totalConsultations = consultationRepository.countByPatientId(patientId);

        AppointmentDTO nextAppt = upcoming.isEmpty() ? null : upcoming.get(0);
        List<ConsultationDTO> recentConsultations = consultationService.getConsultationsByPatientId(patientId).stream()
                .limit(3)
                .collect(Collectors.toList());
        List<PrescriptionDTO> activePrescriptions = prescriptionService.getPrescriptionsByPatientId(patientId).stream()
                .limit(3)
                .collect(Collectors.toList());

        return PatientDashboardDTO.builder()
                .totalAppointments(totalAppointments)
                .upcomingAppointmentsCount(upcomingCount)
                .totalPrescriptions(totalPrescriptions)
                .totalConsultations(totalConsultations)
                .nextUpcomingAppointment(nextAppt)
                .recentConsultations(recentConsultations)
                .activePrescriptions(activePrescriptions)
                .build();
    }
}
