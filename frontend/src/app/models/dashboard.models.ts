import { Appointment, AppointmentStatus } from './appointment.models';
import { Consultation, Prescription } from './consultation.models';

export interface AdminDashboard {
  totalDoctors: number;
  totalPatients: number;
  totalAppointments: number;
  todayAppointmentsCount: number;
  pendingBookingsCount: number;
  statusBreakdown: Record<AppointmentStatus, number>;
  recentAppointments: Appointment[];
}

export interface DoctorDashboard {
  totalAssignedPatients: number;
  todayAppointmentsCount: number;
  upcomingAppointmentsCount: number;
  completedConsultationsCount: number;
  todayAppointments: Appointment[];
  upcomingAppointments: Appointment[];
}

export interface PatientDashboard {
  totalAppointments: number;
  upcomingAppointmentsCount: number;
  totalPrescriptions: number;
  totalConsultations: number;
  nextUpcomingAppointment?: Appointment;
  recentConsultations: Consultation[];
  activePrescriptions: Prescription[];
}
