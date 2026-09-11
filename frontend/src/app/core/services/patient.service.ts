import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Patient, PatientProfileRequest } from '../../models/patient.models';
import { Appointment } from '../../models/appointment.models';
import { Consultation, Prescription } from '../../models/consultation.models';

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/patients';

  getAllPatients(): Observable<Patient[]> {
    return this.http.get<Patient[]>(this.API_URL);
  }

  searchPatients(query?: string): Observable<Patient[]> {
    let params = new HttpParams();
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Patient[]>(`${this.API_URL}/search`, { params });
  }

  getPatientById(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.API_URL}/${id}`);
  }

  getPatientByUserId(userId: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.API_URL}/user/${userId}`);
  }

  updatePatient(id: number, request: PatientProfileRequest): Observable<Patient> {
    return this.http.put<Patient>(`${this.API_URL}/${id}`, request);
  }

  getPatientAppointments(patientId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API_URL}/${patientId}/appointments`);
  }

  getPatientConsultations(patientId: number): Observable<Consultation[]> {
    return this.http.get<Consultation[]>(`${this.API_URL}/${patientId}/consultations`);
  }

  getPatientPrescriptions(patientId: number): Observable<Prescription[]> {
    return this.http.get<Prescription[]>(`${this.API_URL}/${patientId}/prescriptions`);
  }
}
