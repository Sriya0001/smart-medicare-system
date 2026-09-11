export type AppointmentStatus = 'BOOKED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';

export interface Appointment {
  id: number;
  patientId: number;
  patientName: string;
  patientPhone?: string;
  patientEmail?: string;
  doctorId: number;
  doctorName: string;
  doctorSpecialty: string;
  appointmentDate: string;
  startTime: string;
  endTime: string;
  status: AppointmentStatus;
  reason: string;
  notes?: string;
  cancellationReason?: string;
  consultationId?: number;
  createdAt: string;
}

export interface AppointmentCreateRequest {
  doctorId: number;
  patientId?: number;
  appointmentDate: string;
  startTime: string;
  endTime?: string;
  reason: string;
  notes?: string;
}

export interface AppointmentStatusUpdateRequest {
  status: AppointmentStatus;
  cancellationReason?: string;
  notes?: string;
}
