import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DoctorDashboard } from '../../../models/dashboard.models';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, StatCardComponent, StatusBadgeComponent],
  template: `
    <div class="page-wrapper" *ngIf="dashboard">
      <div class="page-header">
        <div>
          <h1 class="page-title">Physician Clinical Dashboard</h1>
          <p class="page-subtitle">Welcome, {{ authService.currentUserValue?.fullName }}</p>
        </div>
        <a routerLink="/doctor/schedule" class="btn btn-outline">
          ⏱ Manage Schedule & Hours
        </a>
      </div>

      <!-- KPI Stat Cards -->
      <div class="grid-4">
        <app-stat-card label="Today's Appointments" [value]="dashboard.todayAppointmentsCount" icon="📅"></app-stat-card>
        <app-stat-card label="Upcoming Schedule" [value]="dashboard.upcomingAppointmentsCount" icon="⏳"></app-stat-card>
        <app-stat-card label="Assigned Patients" [value]="dashboard.totalAssignedPatients" icon="👥"></app-stat-card>
        <app-stat-card label="Completed Consults" [value]="dashboard.completedConsultationsCount" icon="✅"></app-stat-card>
      </div>

      <!-- Today's Clinical Agenda -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">Today's Patient Schedule</span>
          <span class="badge badge-primary">{{ dashboard.todayAppointments.length }} Scheduled Today</span>
        </div>

        <div class="table-responsive" *ngIf="dashboard.todayAppointments && dashboard.todayAppointments.length > 0; else noToday">
          <table class="table">
            <thead>
              <tr>
                <th>Time</th>
                <th>Patient</th>
                <th>Phone</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let appt of dashboard.todayAppointments">
                <td><strong>{{ appt.startTime }} – {{ appt.endTime }}</strong></td>
                <td><strong>{{ appt.patientName }}</strong></td>
                <td>{{ appt.patientPhone || 'N/A' }}</td>
                <td>{{ appt.reason }}</td>
                <td><app-status-badge [status]="appt.status"></app-status-badge></td>
                <td>
                  <a *ngIf="appt.status !== 'COMPLETED'" 
                     [routerLink]="['/doctor/consultation', appt.id]" 
                     class="btn btn-primary btn-sm">
                    🩺 Start Consultation
                  </a>
                  <span *ngIf="appt.status === 'COMPLETED'" class="completed-text">Completed</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noToday>
          <div class="empty-state">
            <div class="empty-state-icon">☕</div>
            <div class="empty-state-title">No Appointments Scheduled Today</div>
            <p>You have no pending consultations on today's clinical agenda.</p>
          </div>
        </ng-template>
      </div>

      <!-- Upcoming Queue Preview -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">Upcoming Appointments Preview</span>
          <a routerLink="/doctor/appointments" class="btn btn-outline btn-sm">View Full Calendar</a>
        </div>

        <div class="table-responsive" *ngIf="dashboard.upcomingAppointments && dashboard.upcomingAppointments.length > 0; else noUpcoming">
          <table class="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Time</th>
                <th>Patient</th>
                <th>Reason</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let appt of dashboard.upcomingAppointments">
                <td><strong>{{ appt.appointmentDate }}</strong></td>
                <td>{{ appt.startTime }} – {{ appt.endTime }}</td>
                <td>{{ appt.patientName }}</td>
                <td>{{ appt.reason }}</td>
                <td><app-status-badge [status]="appt.status"></app-status-badge></td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noUpcoming>
          <div class="empty-state">
            <p>No further upcoming bookings in the queue.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .completed-text {
      font-size: 0.8125rem;
      color: var(--text-muted);
      font-weight: 500;
    }
  `]
})
export class DoctorDashboardComponent implements OnInit {
  authService = inject(AuthService);
  private dashboardService = inject(DashboardService);

  dashboard: DoctorDashboard | null = null;

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.dashboardService.getDoctorDashboard(profileId).subscribe({
        next: (data) => (this.dashboard = data)
      });
    }
  }
}
