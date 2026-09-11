import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AiSummary, Consultation, ConsultationCreateRequest, Prescription, PrescriptionCreateRequest } from '../../models/consultation.models';

@Injectable({
  providedIn: 'root'
})
export class ConsultationService {
  private http = inject(HttpClient);
  private readonly CONSULTATIONS_URL = 'http://localhost:8080/api/consultations';
  private readonly PRESCRIPTIONS_URL = 'http://localhost:8080/api/prescriptions';

  getAllConsultations(): Observable<Consultation[]> {
    return this.http.get<Consultation[]>(this.CONSULTATIONS_URL);
  }

  getConsultationById(id: number): Observable<Consultation> {
    return this.http.get<Consultation>(`${this.CONSULTATIONS_URL}/${id}`);
  }

  getConsultationByAppointmentId(appointmentId: number): Observable<Consultation> {
    return this.http.get<Consultation>(`${this.CONSULTATIONS_URL}/appointment/${appointmentId}`);
  }

  createConsultation(request: ConsultationCreateRequest): Observable<Consultation> {
    return this.http.post<Consultation>(this.CONSULTATIONS_URL, request);
  }

  getPatientHistorySummary(patientId: number): Observable<AiSummary> {
    return this.http.get<AiSummary>(`${this.CONSULTATIONS_URL}/patient/${patientId}/summary`);
  }

  // Prescriptions
  getAllPrescriptions(): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(this.PRESCRIPTIONS_URL);
  }

  getPrescriptionById(id: number): Observable<Prescription> {
    return this.http.get<Prescription>(`${this.PRESCRIPTIONS_URL}/${id}`);
  }

  createPrescription(request: PrescriptionCreateRequest, doctorId?: number): Observable<Prescription> {
    let params = new HttpParams();
    if (doctorId) {
      params = params.set('doctorId', doctorId.toString());
    }
    return this.http.post<Prescription>(this.PRESCRIPTIONS_URL, request, { params });
  }
}
