import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ToastService } from '../../../core/services/toast.service';
import { Appointment } from '../../../models/appointment.models';
import { StatusBadgeComponent } from '../../../shared/components/status-badge/status-badge.component';

@Component({
  selector: 'app-my-appointments',
  standalone: true,
  imports: [CommonModule, RouterModule, StatusBadgeComponent],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">My Appointments</h1>
          <p class="page-subtitle">Track, reschedule, or cancel your practice appointments</p>
        </div>
        <a routerLink="/patient/book-appointment" class="btn btn-primary">+ Book Appointment</a>
      </div>

      <div class="card">
        <div class="table-responsive" *ngIf="appointments && appointments.length > 0; else emptyAppts">
          <table class="table">
            <thead>
              <tr>
                <th>Date & Time</th>
                <th>Doctor</th>
                <th>Specialty</th>
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
                <td>{{ appt.doctorName }}</td>
                <td><span class="badge badge-primary">{{ appt.doctorSpecialty }}</span></td>
                <td>{{ appt.reason }}</td>
                <td><app-status-badge [status]="appt.status"></app-status-badge></td>
                <td>
                  <button *ngIf="appt.status === 'BOOKED' || appt.status === 'CONFIRMED'" 
                          class="btn btn-danger btn-sm" 
                          (click)="cancelAppointment(appt.id)">
                    Cancel
                  </button>
                  <span *ngIf="appt.status === 'COMPLETED'" class="completed-label">Completed</span>
                  <span *ngIf="appt.status === 'CANCELLED'" class="cancelled-label">{{ appt.cancellationReason || 'Cancelled' }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #emptyAppts>
          <div class="empty-state">
            <div class="empty-state-icon">📋</div>
            <div class="empty-state-title">No Appointments on Record</div>
            <p>You haven't scheduled any appointments yet.</p>
            <a routerLink="/patient/book-appointment" class="btn btn-primary btn-sm" style="margin-top: 1rem;">
              Book Your First Visit
            </a>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .time-sub {
      font-size: 0.75rem;
      color: var(--text-muted);
    }
    .completed-label {
      font-size: 0.8125rem;
      color: var(--text-muted);
      font-style: italic;
    }
    .cancelled-label {
      font-size: 0.75rem;
      color: var(--danger);
    }
  `]
})
export class MyAppointmentsComponent implements OnInit {
  private authService = inject(AuthService);
  private patientService = inject(PatientService);
  private appointmentService = inject(AppointmentService);
  private toastService = inject(ToastService);

  appointments: Appointment[] = [];

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.patientService.getPatientAppointments(profileId).subscribe({
        next: (data) => (this.appointments = data)
      });
    }
  }

  cancelAppointment(id: number): void {
    if (confirm('Are you sure you want to cancel this appointment? The slot will be released.')) {
      this.appointmentService.cancelAppointment(id, 'Cancelled by patient').subscribe({
        next: () => {
          this.toastService.info('Appointment cancelled successfully.');
          this.loadAppointments();
        }
      });
    }
  }
}
