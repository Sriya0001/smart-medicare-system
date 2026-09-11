import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ToastService } from '../../../core/services/toast.service';
import { Doctor, AvailableSlot } from '../../../models/doctor.models';

@Component({
  selector: 'app-book-appointment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Book an Appointment</h1>
          <p class="page-subtitle">Select doctor, date, and available time slot to schedule your visit</p>
        </div>
      </div>

      <div class="grid-2">
        <!-- Step 1 & 2: Selection Form -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">1. Choose Doctor & Date</span>
          </div>

          <form [formGroup]="bookingForm">
            <div class="form-group">
              <label class="form-label">Doctor *</label>
              <select class="form-select" formControlName="doctorId" (change)="onDoctorOrDateChange()">
                <option value="">Select a Doctor</option>
                <option *ngFor="let doc of doctors" [value]="doc.id">
                  {{ doc.fullName }} — {{ doc.specialty }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Appointment Date *</label>
              <input type="date" class="form-control" formControlName="appointmentDate" [min]="minDate" (change)="onDoctorOrDateChange()">
            </div>

            <div class="form-group">
              <label class="form-label">Reason for Visit *</label>
              <input type="text" class="form-control" formControlName="reason" placeholder="e.g. Annual physical, Follow-up consultation, Joint pain">
            </div>

            <div class="form-group">
              <label class="form-label">Additional Notes for Doctor (Optional)</label>
              <textarea class="form-control" formControlName="notes" placeholder="Describe symptoms or background notes..."></textarea>
            </div>
          </form>
        </div>

        <!-- Step 3: Available Time Slots Picker -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">2. Select Available Time Slot</span>
            <span class="slot-count" *ngIf="availableSlots.length > 0">
              {{ getAvailableCount() }} slots available
            </span>
          </div>

          <div *ngIf="loadingSlots" class="empty-state">
            <div class="empty-state-icon">⏳</div>
            <p>Checking doctor availability...</p>
          </div>

          <div *ngIf="!loadingSlots && availableSlots.length > 0">
            <p class="slots-instruction">Click an open time slot below:</p>
            <div class="slots-grid">
              <button *ngFor="let slot of availableSlots" 
                      type="button" 
                      class="slot-btn" 
                      [class.selected]="selectedSlot?.startTime === slot.startTime" 
                      [disabled]="!slot.available" 
                      (click)="selectSlot(slot)">
                {{ slot.startTime }}
              </button>
            </div>

            <div class="slot-legend">
              <span class="legend-item"><span class="dot dot-available"></span> Available</span>
              <span class="legend-item"><span class="dot dot-selected"></span> Selected</span>
              <span class="legend-item"><span class="dot dot-booked"></span> Booked / Past</span>
            </div>
          </div>

          <div *ngIf="!loadingSlots && availableSlots.length === 0 && bookingForm.get('doctorId')?.value && bookingForm.get('appointmentDate')?.value">
            <div class="empty-state">
              <div class="empty-state-icon">🚫</div>
              <div class="empty-state-title">No Available Slots</div>
              <p>The selected doctor has no available practice hours or all slots are booked on this date. Please pick another date.</p>
            </div>
          </div>

          <div *ngIf="!bookingForm.get('doctorId')?.value || !bookingForm.get('appointmentDate')?.value">
            <div class="empty-state">
              <div class="empty-state-icon">👈</div>
              <div class="empty-state-title">Select Doctor & Date</div>
              <p>Please select a doctor and appointment date to calculate available slots.</p>
            </div>
          </div>

          <!-- Booking Submission Button -->
          <div class="book-action" *ngIf="selectedSlot">
            <div class="selected-summary">
              Selected: <strong>{{ bookingForm.get('appointmentDate')?.value }}</strong> at <strong>{{ selectedSlot.startTime }} – {{ selectedSlot.endTime }}</strong>
            </div>
            <button class="btn btn-primary btn-block" [disabled]="bookingForm.invalid || submitting" (click)="confirmBooking()">
              <span *ngIf="!submitting">Confirm & Book Appointment</span>
              <span *ngIf="submitting">Booking...</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .slot-count {
      font-size: 0.8125rem;
      color: var(--accent);
      font-weight: 600;
    }
    .slots-instruction {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-bottom: 0.5rem;
    }
    .slot-legend {
      display: flex;
      gap: 1rem;
      margin-top: 1.25rem;
      padding-top: 0.75rem;
      border-top: 1px solid var(--border);
      font-size: 0.75rem;
      color: var(--text-muted);
    }
    .legend-item {
      display: flex;
      align-items: center;
      gap: 0.375rem;
    }
    .dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      display: inline-block;
    }
    .dot-available { background-color: var(--primary); }
    .dot-selected { background-color: var(--secondary); }
    .dot-booked { background-color: #cbd5e1; }
    .book-action {
      margin-top: 1.5rem;
      padding-top: 1rem;
      border-top: 1px solid var(--border);
    }
    .selected-summary {
      background-color: var(--primary-light);
      color: var(--primary-hover);
      padding: 0.75rem;
      border-radius: var(--radius-sm);
      font-size: 0.875rem;
      margin-bottom: 1rem;
      text-align: center;
    }
    .btn-block {
      width: 100%;
    }
  `]
})
export class BookAppointmentComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private authService = inject(AuthService);
  private doctorService = inject(DoctorService);
  private appointmentService = inject(AppointmentService);
  private toastService = inject(ToastService);

  doctors: Doctor[] = [];
  availableSlots: AvailableSlot[] = [];
  selectedSlot: AvailableSlot | null = null;
  loadingSlots = false;
  submitting = false;
  minDate = '';

  bookingForm = this.fb.group({
    doctorId: ['', Validators.required],
    appointmentDate: ['', Validators.required],
    reason: ['', Validators.required],
    notes: ['']
  });

  ngOnInit(): void {
    const today = new Date();
    this.minDate = today.toISOString().split('T')[0];

    this.doctorService.getAllDoctors().subscribe({
      next: (data) => {
        this.doctors = data;
        // Check queryParams for pre-selected doctor
        const paramDocId = this.route.snapshot.queryParams['doctorId'];
        if (paramDocId) {
          this.bookingForm.patchValue({ doctorId: paramDocId });
        }
      }
    });
  }

  onDoctorOrDateChange(): void {
    const docId = Number(this.bookingForm.get('doctorId')?.value);
    const date = this.bookingForm.get('appointmentDate')?.value;
    this.selectedSlot = null;

    if (docId && date) {
      this.loadingSlots = true;
      this.appointmentService.getAvailableSlots(docId, date).subscribe({
        next: (slots) => {
          this.availableSlots = slots;
          this.loadingSlots = false;
        },
        error: () => {
          this.availableSlots = [];
          this.loadingSlots = false;
        }
      });
    } else {
      this.availableSlots = [];
    }
  }

  getAvailableCount(): number {
    return this.availableSlots.filter(s => s.available).length;
  }

  selectSlot(slot: AvailableSlot): void {
    if (!slot.available) return;
    this.selectedSlot = slot;
  }

  confirmBooking(): void {
    if (this.bookingForm.invalid || !this.selectedSlot) return;

    const patientProfileId = this.authService.profileId;
    if (!patientProfileId) {
      this.toastService.error('Patient profile not resolved. Please login again.');
      return;
    }

    this.submitting = true;
    const formVal = this.bookingForm.getRawValue();

    this.appointmentService.createAppointment({
      doctorId: Number(formVal.doctorId),
      patientId: patientProfileId,
      appointmentDate: formVal.appointmentDate!,
      startTime: this.selectedSlot.startTime,
      endTime: this.selectedSlot.endTime,
      reason: formVal.reason!,
      notes: formVal.notes || undefined
    }).subscribe({
      next: (appt) => {
        this.submitting = false;
        this.toastService.success(`Appointment confirmed with ${appt.doctorName} on ${appt.appointmentDate} at ${appt.startTime}`);
        this.router.navigate(['/patient/appointments']);
      },
      error: () => {
        this.submitting = false;
        // Re-calculate slots in case the slot was taken concurrently
        this.onDoctorOrDateChange();
      }
    });
  }
}
