import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ToastService } from '../../../core/services/toast.service';
import { Appointment, AppointmentStatus } from '../../../models/appointment.models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-appointment-management',
  standalone: true,
  imports: [CommonModule, FormsModule, StatusBadgeComponent],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Practice Appointments Master Log</h1>
          <p class="page-subtitle">Supervise practice-wide scheduling, confirm bookings, and manage statuses</p>
        </div>
      </div>

      <div class="card filter-card">
        <div class="filter-row">
          <input type="text" class="form-control search-input" [(ngModel)]="filterQuery" placeholder="Filter by patient, doctor, or specialty...">
          <select class="form-select filter-select" [(ngModel)]="statusFilter">
            <option value="">All Statuses</option>
            <option value="BOOKED">Booked (Pending)</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="COMPLETED">Completed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
        </div>
      </div>

      <div class="card">
        <div class="table-responsive" *ngIf="filteredAppointments.length > 0; else noAppts">
          <table class="table">
            <thead>
              <tr>
                <th>Appt #</th>
                <th>Date & Time</th>
                <th>Patient</th>
                <th>Doctor</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Admin Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let appt of filteredAppointments">
                <td><strong>#{{ appt.id }}</strong></td>
                <td>
                  <strong>{{ appt.appointmentDate }}</strong>
                  <div class="time-sub">{{ appt.startTime }} – {{ appt.endTime }}</div>
                </td>
                <td>
                  <div>{{ appt.patientName }}</div>
                  <div class="time-sub">{{ appt.patientPhone }}</div>
                </td>
                <td>
                  <div>{{ appt.doctorName }}</div>
                  <div class="time-sub">{{ appt.doctorSpecialty }}</div>
                </td>
                <td>{{ appt.reason }}</td>
                <td><app-status-badge [status]="appt.status"></app-status-badge></td>
                <td>
                  <div class="actions">
                    <button *ngIf="appt.status === 'BOOKED'" 
                            class="btn btn-success btn-sm" 
                            (click)="updateStatus(appt.id, 'CONFIRMED')">
                      Confirm
                    </button>
                    <button *ngIf="appt.status === 'BOOKED' || appt.status === 'CONFIRMED'" 
                            class="btn btn-danger btn-sm" 
                            (click)="cancelAppointment(appt.id)">
                      Cancel
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noAppts>
          <div class="empty-state">
            <p>No appointments match the given filter criteria.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .filter-card {
      padding: 1rem;
      margin-bottom: 1.5rem;
    }
    .filter-row {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }
    .search-input {
      flex: 2;
      min-width: 240px;
    }
    .filter-select {
      flex: 1;
      min-width: 180px;
    }
    .time-sub {
      font-size: 0.75rem;
      color: var(--text-muted);
    }
    .actions {
      display: flex;
      gap: 0.5rem;
    }
  `]
})
export class AppointmentManagementComponent implements OnInit {
  private appointmentService = inject(AppointmentService);
  private toastService = inject(ToastService);

  appointments: Appointment[] = [];
  filterQuery = '';
  statusFilter = '';

  get filteredAppointments(): Appointment[] {
    return this.appointments.filter(appt => {
      const matchesQuery = !this.filterQuery ||
        appt.patientName.toLowerCase().includes(this.filterQuery.toLowerCase()) ||
        appt.doctorName.toLowerCase().includes(this.filterQuery.toLowerCase()) ||
        appt.doctorSpecialty.toLowerCase().includes(this.filterQuery.toLowerCase()) ||
        appt.reason.toLowerCase().includes(this.filterQuery.toLowerCase());

      const matchesStatus = !this.statusFilter || appt.status === this.statusFilter;

      return matchesQuery && matchesStatus;
    });
  }

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments(): void {
    this.appointmentService.getAllAppointments().subscribe({
      next: (data) => (this.appointments = data)
    });
  }

  updateStatus(id: number, status: AppointmentStatus): void {
    this.appointmentService.updateAppointmentStatus(id, { status }).subscribe({
      next: () => {
        this.toastService.success(`Appointment #${id} updated to ${status}`);
        this.loadAppointments();
      }
    });
  }

  cancelAppointment(id: number): void {
    if (confirm(`Cancel appointment #${id}? The slot will be freed for re-booking.`)) {
      this.appointmentService.cancelAppointment(id, 'Cancelled by practice administrator').subscribe({
        next: () => {
          this.toastService.info(`Appointment #${id} cancelled.`);
          this.loadAppointments();
        }
      });
    }
  }
}
