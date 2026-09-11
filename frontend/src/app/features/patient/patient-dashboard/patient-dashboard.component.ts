import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DashboardService } from '../../../core/services/dashboard.service';
import { PatientDashboard } from '../../../models/dashboard.models';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, StatCardComponent, StatusBadgeComponent],
  template: `
    <div class="page-wrapper" *ngIf="dashboard">
      <div class="page-header">
        <div>
          <h1 class="page-title">Patient Portal</h1>
          <p class="page-subtitle">Welcome back, {{ authService.currentUserValue?.fullName }}</p>
        </div>
        <a routerLink="/patient/book-appointment" class="btn btn-primary">
          + Book New Appointment
        </a>
      </div>

      <!-- KPI Stat Cards -->
      <div class="grid-4">
        <app-stat-card label="Upcoming Appts" [value]="dashboard.upcomingAppointmentsCount" icon="📅"></app-stat-card>
        <app-stat-card label="Total Consultations" [value]="dashboard.totalConsultations" icon="📑"></app-stat-card>
        <app-stat-card label="Active Prescriptions" [value]="dashboard.totalPrescriptions" icon="💊"></app-stat-card>
        <app-stat-card label="All-Time Visits" [value]="dashboard.totalAppointments" icon="🩺"></app-stat-card>
      </div>

      <!-- Next Upcoming Appointment Banner -->
      <div class="card next-appt-card" *ngIf="dashboard.nextUpcomingAppointment as appt; else noNextAppt">
        <div class="card-header">
          <span class="card-title">Next Scheduled Appointment</span>
          <app-status-badge [status]="appt.status"></app-status-badge>
        </div>
        <div class="appt-banner-content">
          <div class="appt-doctor-info">
            <h3>{{ appt.doctorName }}</h3>
            <p class="doctor-spec">{{ appt.doctorSpecialty }}</p>
            <p class="appt-reason"><strong>Reason:</strong> {{ appt.reason }}</p>
          </div>
          <div class="appt-time-info">
            <div class="time-box">
              <span class="date-str">{{ appt.appointmentDate }}</span>
              <span class="time-str">{{ appt.startTime }} – {{ appt.endTime }}</span>
            </div>
            <a [routerLink]="['/patient/appointments']" class="btn btn-outline btn-sm">Manage</a>
          </div>
        </div>
      </div>

      <ng-template #noNextAppt>
        <div class="card empty-banner">
          <div class="empty-state">
            <div class="empty-state-icon">📅</div>
            <div class="empty-state-title">No Upcoming Appointments</div>
            <p>You have no pending visits scheduled. Select a doctor to book a visit.</p>
            <a routerLink="/patient/book-appointment" class="btn btn-primary btn-sm" style="margin-top: 1rem;">
              Find Available Doctors
            </a>
          </div>
        </div>
      </ng-template>

      <!-- Recent Consultations & Prescriptions Grid -->
      <div class="grid-2">
        <!-- Recent Clinical Records -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">Recent Clinical Notes</span>
            <a routerLink="/patient/medical-history" class="btn btn-outline btn-sm">View All</a>
          </div>
          <div *ngIf="dashboard.recentConsultations && dashboard.recentConsultations.length > 0; else noCons">
            <div class="item-list">
              <div class="list-item" *ngFor="let cons of dashboard.recentConsultations">
                <div class="item-main">
                  <strong>{{ cons.diagnosis }}</strong>
                  <div class="item-sub">By {{ cons.doctorName }} ({{ cons.doctorSpecialty }})</div>
                  <div class="item-desc">{{ cons.symptoms }}</div>
                </div>
                <div class="item-date">{{ cons.consultationDate | date:'mediumDate' }}</div>
              </div>
            </div>
          </div>
          <ng-template #noCons>
            <div class="empty-state">No clinical notes recorded yet.</div>
          </ng-template>
        </div>

        <!-- Active Prescriptions -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">Recent Prescriptions</span>
            <a routerLink="/patient/prescriptions" class="btn btn-outline btn-sm">View All</a>
          </div>
          <div *ngIf="dashboard.activePrescriptions && dashboard.activePrescriptions.length > 0; else noRx">
            <div class="item-list">
              <div class="list-item" *ngFor="let rx of dashboard.activePrescriptions">
                <div class="item-main">
                  <strong>Prescribed by {{ rx.doctorName }}</strong>
                  <div class="rx-items" *ngFor="let item of rx.items">
                    • {{ item.medicationName }} ({{ item.dosage }}) — {{ item.frequency }} for {{ item.duration }}
                  </div>
                </div>
                <div class="item-date">{{ rx.issueDate }}</div>
              </div>
            </div>
          </div>
          <ng-template #noRx>
            <div class="empty-state">No active prescriptions on file.</div>
          </ng-template>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .next-appt-card {
      border-left: 4px solid var(--primary);
    }
    .appt-banner-content {
      display: flex;
      justify-content: space-between;
      align-items: center;
      flex-wrap: wrap;
      gap: 1.5rem;
    }
    .doctor-spec {
      color: var(--primary);
      font-weight: 500;
      font-size: 0.875rem;
      margin-bottom: 0.375rem;
    }
    .appt-reason {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    .appt-time-info {
      display: flex;
      align-items: center;
      gap: 1.5rem;
    }
    .time-box {
      background-color: var(--primary-light);
      padding: 0.75rem 1.25rem;
      border-radius: var(--radius-sm);
      text-align: center;
      display: flex;
      flex-direction: column;
    }
    .date-str {
      font-weight: 700;
      color: var(--secondary);
      font-size: 0.9375rem;
    }
    .time-str {
      font-size: 0.8125rem;
      color: var(--primary-hover);
      font-weight: 600;
    }
    .item-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }
    .list-item {
      padding-bottom: 0.875rem;
      border-bottom: 1px solid var(--border);
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 1rem;
    }
    .list-item:last-child {
      border-bottom: none;
      padding-bottom: 0;
    }
    .item-sub {
      font-size: 0.75rem;
      color: var(--text-muted);
      margin: 0.125rem 0;
    }
    .item-desc {
      font-size: 0.8125rem;
      color: var(--text-main);
    }
    .item-date {
      font-size: 0.75rem;
      color: var(--text-sub);
      white-space: nowrap;
    }
    .rx-items {
      font-size: 0.8125rem;
      color: var(--secondary);
      margin-top: 0.25rem;
    }
  `]
})
export class PatientDashboardComponent implements OnInit {
  authService = inject(AuthService);
  private dashboardService = inject(DashboardService);

  dashboard: PatientDashboard | null = null;

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.dashboardService.getPatientDashboard(profileId).subscribe({
        next: (data) => (this.dashboard = data)
      });
    }
  }
}
