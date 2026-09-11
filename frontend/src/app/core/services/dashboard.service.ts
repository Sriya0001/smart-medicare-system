import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdminDashboard, DoctorDashboard, PatientDashboard } from '../../models/dashboard.models';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/api/dashboard';

  getAdminDashboard(): Observable<AdminDashboard> {
    return this.http.get<AdminDashboard>(`${this.API_URL}/admin`);
  }

  getDoctorDashboard(doctorId: number): Observable<DoctorDashboard> {
    return this.http.get<DoctorDashboard>(`${this.API_URL}/doctor/${doctorId}`);
  }

  getPatientDashboard(patientId: number): Observable<PatientDashboard> {
    return this.http.get<PatientDashboard>(`${this.API_URL}/patient/${patientId}`);
  }
}
