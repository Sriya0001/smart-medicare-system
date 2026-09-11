export interface DoctorAvailability {
  id?: number;
  doctorId?: number;
  doctorName?: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  startTime: string;
  endTime: string;
  slotDurationMinutes: number;
  active: boolean;
}

export interface AvailableSlot {
  date: string;
  startTime: string;
  endTime: string;
  available: boolean;
  reason: string;
}

export interface Doctor {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  specialty: string;
  licenseNumber: string;
  phoneNumber?: string;
  experienceYears?: number;
  biography?: string;
  consultationFee?: number;
  active: boolean;
  availabilities?: DoctorAvailability[];
}

export interface DoctorCreateRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  specialty: string;
  licenseNumber: string;
  phoneNumber?: string;
  experienceYears?: number;
  biography?: string;
  consultationFee?: number;
}

export interface DoctorUpdateRequest {
  firstName: string;
  lastName: string;
  specialty: string;
  licenseNumber?: string;
  phoneNumber?: string;
  experienceYears?: number;
  biography?: string;
  consultationFee?: number;
  active?: boolean;
}
