package com.medicare.practice.dto;

import com.medicare.practice.entity.AppointmentStatus;
import lombok.*;

import java.util.List;
import java.util.Map;

public class DashboardDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AdminDashboardDTO {
        private long totalDoctors;
        private long totalPatients;
        private long totalAppointments;
        private long todayAppointmentsCount;
        private long pendingBookingsCount;
        private Map<AppointmentStatus, Long> statusBreakdown;
        private List<AppointmentDTOs.AppointmentDTO> recentAppointments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DoctorDashboardDTO {
        private long totalAssignedPatients;
        private long todayAppointmentsCount;
        private long upcomingAppointmentsCount;
        private long completedConsultationsCount;
        private List<AppointmentDTOs.AppointmentDTO> todayAppointments;
        private List<AppointmentDTOs.AppointmentDTO> upcomingAppointments;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PatientDashboardDTO {
        private long totalAppointments;
        private long upcomingAppointmentsCount;
        private long totalPrescriptions;
        private long totalConsultations;
        private AppointmentDTOs.AppointmentDTO nextUpcomingAppointment;
        private List<ConsultationDTOs.ConsultationDTO> recentConsultations;
        private List<PrescriptionDTOs.PrescriptionDTO> activePrescriptions;
    }
}
