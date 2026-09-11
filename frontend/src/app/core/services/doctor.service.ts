import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AvailableSlot, Doctor, DoctorAvailability, DoctorCreateRequest, DoctorUpdateRequest } from '../../models/doctor.models';
import { Appointment } from '../../models/appointment.models';
import { Patient } from '../../models/patient.models';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/doctors';

  getAllDoctors(specialty?: string): Observable<Doctor[]> {
    let params = new HttpParams();
    if (specialty && specialty.trim()) {
      params = params.set('specialty', specialty.trim());
    }
    return this.http.get<Doctor[]>(this.API_URL, { params });
  }

  getAllDoctorsForAdmin(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(`${this.API_URL}/admin-all`);
  }

  getAllSpecialties(): Observable<string[]> {
    return this.http.get<string[]>(`${this.API_URL}/specialties`);
  }

  searchDoctors(query?: string): Observable<Doctor[]> {
    let params = new HttpParams();
    if (query && query.trim()) {
      params = params.set('query', query.trim());
    }
    return this.http.get<Doctor[]>(`${this.API_URL}/search`, { params });
  }

  getDoctorById(id: number): Observable<Doctor> {
    return this.http.get<Doctor>(`${this.API_URL}/${id}`);
  }

  getDoctorByUserId(userId: number): Observable<Doctor> {
    return this.http.get<Doctor>(`${this.API_URL}/user/${userId}`);
  }

  createDoctor(request: DoctorCreateRequest): Observable<Doctor> {
    return this.http.post<Doctor>(this.API_URL, request);
  }

  updateDoctor(id: number, request: DoctorUpdateRequest): Observable<Doctor> {
    return this.http.put<Doctor>(`${this.API_URL}/${id}`, request);
  }

  deactivateDoctor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getDoctorAvailability(doctorId: number): Observable<DoctorAvailability[]> {
    return this.http.get<DoctorAvailability[]>(`${this.API_URL}/${doctorId}/availability`);
  }

  setDoctorAvailability(doctorId: number, request: Partial<DoctorAvailability>): Observable<DoctorAvailability> {
    return this.http.post<DoctorAvailability>(`${this.API_URL}/${doctorId}/availability`, request);
  }

  getAvailableSlots(doctorId: number, date: string): Observable<AvailableSlot[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<AvailableSlot[]>(`${this.API_URL}/${doctorId}/available-slots`, { params });
  }

  getDoctorAppointments(doctorId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API_URL}/${doctorId}/appointments`);
  }

  getTodayAppointments(doctorId: number): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.API_URL}/${doctorId}/today-appointments`);
  }

  getDoctorPatients(doctorId: number): Observable<Patient[]> {
    return this.http.get<Patient[]>(`${this.API_URL}/${doctorId}/patients`);
  }
}
