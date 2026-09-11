import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService } from '../../../core/services/dashboard.service';
import { AdminDashboard } from '../../../models/dashboard.models';
import { StatCardComponent } from '../../../shared/components/stat-card/stat-card.component';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, StatCardComponent, StatusBadgeComponent],
  template: `
    <div class="page-wrapper" *ngIf="dashboard">
      <div class="page-header">
        <div>
          <h1 class="page-title">Practice Administration Hub</h1>
          <p class="page-subtitle">Practice analytics, capacity management, and operations dashboard</p>
        </div>
        <div class="header-actions">
          <a routerLink="/admin/doctors" class="btn btn-primary">+ Manage Doctors</a>
        </div>
      </div>

      <!-- Main Operational KPIs -->
      <div class="grid-4">
        <app-stat-card label="Total Doctors" [value]="dashboard.totalDoctors" icon="🩺"></app-stat-card>
        <app-stat-card label="Total Patients" [value]="dashboard.totalPatients" icon="👥"></app-stat-card>
        <app-stat-card label="Total Appointments" [value]="dashboard.totalAppointments" icon="📊"></app-stat-card>
        <app-stat-card label="Today's Appointments" [value]="dashboard.todayAppointmentsCount" icon="📅"></app-stat-card>
      </div>

      <!-- Practice Status Breakdown Cards -->
      <div class="grid-2">
        <div class="card">
          <div class="card-header">
            <span class="card-title">Appointment Lifecycle Status Breakdown</span>
          </div>
          <div class="status-breakdown-grid" *ngIf="dashboard.statusBreakdown">
            <div class="breakdown-box">
              <span class="badge badge-primary">BOOKED</span>
              <div class="breakdown-num">{{ dashboard.statusBreakdown['BOOKED'] || 0 }}</div>
              <span class="breakdown-sub">Awaiting confirmation</span>
            </div>
            <div class="breakdown-box">
              <span class="badge badge-success">CONFIRMED</span>
              <div class="breakdown-num">{{ dashboard.statusBreakdown['CONFIRMED'] || 0 }}</div>
              <span class="breakdown-sub">Confirmed slots</span>
            </div>
            <div class="breakdown-box">
              <span class="badge badge-neutral">COMPLETED</span>
              <div class="breakdown-num">{{ dashboard.statusBreakdown['COMPLETED'] || 0 }}</div>
              <span class="breakdown-sub">Consultations finished</span>
            </div>
            <div class="breakdown-box">
              <span class="badge badge-danger">CANCELLED</span>
              <div class="breakdown-num">{{ dashboard.statusBreakdown['CANCELLED'] || 0 }}</div>
              <span class="breakdown-sub">Released slots</span>
            </div>
          </div>
        </div>

        <!-- Quick Administration Controls -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">Practice Administration Quick Links</span>
          </div>
          <div class="quick-links">
            <a routerLink="/admin/doctors" class="quick-link-item">
              <span class="icon">🩺</span>
              <div>
                <strong>Doctor Directory & Credentials</strong>
                <p>Add new clinicians, configure working hours, and manage practice specialties.</p>
              </div>
            </a>
            <a routerLink="/admin/patients" class="quick-link-item">
              <span class="icon">👥</span>
              <div>
                <strong>Patient Registry</strong>
                <p>Look up patient profiles, demographics, and contact info across the practice.</p>
              </div>
            </a>
            <a routerLink="/admin/appointments" class="quick-link-item">
              <span class="icon">📅</span>
              <div>
                <strong>Master Practice Calendar</strong>
                <p>Supervise all upcoming visits, override statuses, or handle cancellations.</p>
              </div>
            </a>
          </div>
        </div>
      </div>

      <!-- Recent Practice Appointments Table -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">Recent Practice Appointments</span>
          <a routerLink="/admin/appointments" class="btn btn-outline btn-sm">View Master Log</a>
        </div>

        <div class="table-responsive" *ngIf="dashboard.recentAppointments && dashboard.recentAppointments.length > 0; else noRecent">
          <table class="table">
            <thead>
              <tr>
                <th>Date & Time</th>
                <th>Patient</th>
                <th>Doctor</th>
                <th>Reason</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let appt of dashboard.recentAppointments">
                <td><strong>{{ appt.appointmentDate }}</strong> ({{ appt.startTime }} – {{ appt.endTime }})</td>
                <td>{{ appt.patientName }}</td>
                <td>{{ appt.doctorName }} ({{ appt.doctorSpecialty }})</td>
                <td>{{ appt.reason }}</td>
                <td><app-status-badge [status]="appt.status"></app-status-badge></td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noRecent>
          <div class="empty-state">
            <p>No appointments recorded in the practice database yet.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .status-breakdown-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
    }
    .breakdown-box {
      background-color: #f8fafc;
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      padding: 1rem;
      text-align: center;
    }
    .breakdown-num {
      font-size: 1.75rem;
      font-weight: 800;
      color: var(--secondary);
      margin: 0.375rem 0;
    }
    .breakdown-sub {
      font-size: 0.75rem;
      color: var(--text-muted);
    }
    .quick-links {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }
    .quick-link-item {
      display: flex;
      gap: 1rem;
      align-items: flex-start;
      padding: 0.875rem;
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      text-decoration: none;
      color: inherit;
      transition: all 0.15s ease;
    }
    .quick-link-item:hover {
      border-color: var(--primary);
      background-color: var(--primary-light);
    }
    .quick-link-item .icon {
      font-size: 1.5rem;
      flex-shrink: 0;
    }
    .quick-link-item strong {
      font-size: 0.875rem;
      color: var(--secondary);
      display: block;
    }
    .quick-link-item p {
      font-size: 0.75rem;
      color: var(--text-muted);
      margin-top: 0.125rem;
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  dashboard: AdminDashboard | null = null;

  ngOnInit(): void {
    this.dashboardService.getAdminDashboard().subscribe({
      next: (data) => (this.dashboard = data)
    });
  }
}
