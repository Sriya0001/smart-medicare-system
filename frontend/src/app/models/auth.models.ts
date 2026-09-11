export type Role = 'ROLE_ADMIN' | 'ROLE_DOCTOR' | 'ROLE_PATIENT';

export interface User {
  id: number;
  email: string;
  role: Role;
  enabled: boolean;
}

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  email: string;
  role: Role;
  profileId: number | null;
  fullName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: Role;
  dateOfBirth?: string;
  gender?: string;
  phoneNumber?: string;
  address?: string;
  bloodGroup?: string;
  emergencyContact?: string;
  specialty?: string;
  licenseNumber?: string;
  experienceYears?: number;
}
