import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Appointment, AppointmentCreateRequest, AppointmentStatusUpdateRequest } from '../../models/appointment.models';
import { AvailableSlot } from '../../models/doctor.models';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/appointments';

  getAllAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(this.API_URL);
  }

  getAppointmentById(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(`${this.API_URL}/${id}`);
  }

  getAvailableSlots(doctorId: number, date: string): Observable<AvailableSlot[]> {
    const params = new HttpParams()
      .set('doctorId', doctorId.toString())
      .set('date', date);
    return this.http.get<AvailableSlot[]>(`${this.API_URL}/available-slots`, { params });
  }

  createAppointment(request: AppointmentCreateRequest): Observable<Appointment> {
    return this.http.post<Appointment>(this.API_URL, request);
  }

  updateAppointmentStatus(id: number, request: AppointmentStatusUpdateRequest): Observable<Appointment> {
    return this.http.patch<Appointment>(`${this.API_URL}/${id}/status`, request);
  }

  cancelAppointment(id: number, reason?: string): Observable<Appointment> {
    let params = new HttpParams();
    if (reason && reason.trim()) {
      params = params.set('reason', reason.trim());
    }
    return this.http.post<Appointment>(`${this.API_URL}/${id}/cancel`, {}, { params });
  }
}
