export interface Patient {
  id: number;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  fullName: string;
  dateOfBirth?: string;
  gender?: string;
  phoneNumber?: string;
  address?: string;
  bloodGroup?: string;
  emergencyContact?: string;
  createdAt: string;
}

export interface PatientProfileRequest {
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: string;
  phoneNumber?: string;
  address?: string;
  bloodGroup?: string;
  emergencyContact?: string;
}
