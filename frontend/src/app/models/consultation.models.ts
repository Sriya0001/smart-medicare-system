export interface PrescriptionItem {
  id?: number;
  medicationName: string;
  dosage: string;
  frequency: string;
  duration: string;
  instructions?: string;
}

export interface Prescription {
  id: number;
  consultationId?: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  issueDate: string;
  notes?: string;
  items: PrescriptionItem[];
  createdAt: string;
}

export interface PrescriptionCreateRequest {
  consultationId?: number;
  patientId: number;
  items: PrescriptionItem[];
  notes?: string;
}

export interface Consultation {
  id: number;
  appointmentId: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  doctorSpecialty: string;
  consultationDate: string;
  symptoms: string;
  diagnosis: string;
  treatmentPlan?: string;
  notes?: string;
  followUpDate?: string;
  prescriptions?: Prescription[];
  createdAt: string;
}

export interface ConsultationCreateRequest {
  appointmentId: number;
  symptoms: string;
  diagnosis: string;
  treatmentPlan?: string;
  notes?: string;
  followUpDate?: string;
  prescriptionItems?: PrescriptionItem[];
}

export interface AiSummary {
  patientId: number;
  patientName: string;
  totalConsultationsAnalyzed: number;
  chiefComplaintsSummary: string;
  diagnosisHistory: string;
  treatmentsSummary: string;
  followUpDirectives: string;
  rawExecutiveSummary: string;
  isAiGenerated: boolean;
}
