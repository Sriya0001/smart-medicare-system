import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ToastService } from '../../../core/services/toast.service';
import { Appointment } from '../../../models/appointment.models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [CommonModule, RouterModule, StatusBadgeComponent],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Appointment Calendar & Schedule</h1>
          <p class="page-subtitle">Manage, confirm, and initiate clinical encounters for all patient bookings</p>
        </div>
      </div>

      <div class="card">
        <div class="table-responsive" *ngIf="appointments && appointments.length > 0; else noAppts">
          <table class="table">
            <thead>
              <tr>
                <th>Date & Time</th>
                <th>Patient</th>
                <th>Contact</th>
                <th>Reason</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let appt of appointments">
                <td>
                  <strong>{{ appt.appointmentDate }}</strong>
                  <div class="time-sub">{{ appt.startTime }} – {{ appt.endTime }}</div>
                </td>
                <td>
                  <strong>{{ appt.patientName }}</strong>
                </td>
                <td>
                  <div>{{ appt.patientPhone || 'N/A' }}</div>
                  <div class="email-sub">{{ appt.patientEmail }}</div>
                </td>
                <td>{{ appt.reason }}</td>
                <td><app-status-badge [status]="appt.status"></app-status-badge></td>
                <td>
                  <div class="action-buttons">
                    <button *ngIf="appt.status === 'BOOKED'" 
                            class="btn btn-success btn-sm" 
                            (click)="updateStatus(appt.id, 'CONFIRMED')">
                      Confirm
                    </button>
                    <a *ngIf="appt.status !== 'COMPLETED' && appt.status !== 'CANCELLED'" 
                       [routerLink]="['/doctor/consultation', appt.id]" 
                       class="btn btn-primary btn-sm">
                      Consultation
                    </a>
                    <button *ngIf="appt.status === 'BOOKED' || appt.status === 'CONFIRMED'" 
                            class="btn btn-danger btn-sm" 
                            (click)="cancelAppointment(appt.id)">
                      Cancel
                    </button>
                    <span *ngIf="appt.status === 'COMPLETED'" class="completed-label">Completed</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noAppts>
          <div class="empty-state">
            <div class="empty-state-icon">📅</div>
            <div class="empty-state-title">No Appointments on Record</div>
            <p>You have no scheduled patient appointments in the system.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .time-sub, .email-sub {
      font-size: 0.75rem;
      color: var(--text-muted);
    }
    .action-buttons {
      display: flex;
      gap: 0.5rem;
      align-items: center;
      flex-wrap: wrap;
    }
    .completed-label {
      font-size: 0.8125rem;
      color: var(--text-muted);
      font-style: italic;
    }
  `]
})
export class DoctorAppointmentsComponent implements OnInit {
  private authService = inject(AuthService);
  private doctorService = inject(DoctorService);
  private appointmentService = inject(AppointmentService);
  private toastService = inject(ToastService);

  appointments: Appointment[] = [];

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.doctorService.getDoctorAppointments(profileId).subscribe({
        next: (data) => (this.appointments = data)
      });
    }
  }

  updateStatus(id: number, status: 'CONFIRMED'): void {
    this.appointmentService.updateAppointmentStatus(id, { status }).subscribe({
      next: () => {
        this.toastService.success('Appointment status updated to ' + status);
        this.loadAppointments();
      }
    });
  }

  cancelAppointment(id: number): void {
    if (confirm('Cancel this appointment? The patient will be notified and slot released.')) {
      this.appointmentService.cancelAppointment(id, 'Cancelled by physician schedule adjustment').subscribe({
        next: () => {
          this.toastService.info('Appointment cancelled.');
          this.loadAppointments();
        }
      });
    }
  }
}
