import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { ToastService } from '../../../core/services/toast.service';
import { DoctorAvailability } from '../../../models/doctor.models';

@Component({
  selector: 'app-doctor-schedule',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Working Hours & Availability Schedule</h1>
          <p class="page-subtitle">Configure your recurring weekly clinical practice hours for smart slot generation</p>
        </div>
      </div>

      <div class="grid-2">
        <!-- Current Schedule List -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">Weekly Recurring Hours</span>
          </div>

          <div class="table-responsive" *ngIf="availabilities && availabilities.length > 0; else noSched">
            <table class="table">
              <thead>
                <tr>
                  <th>Day</th>
                  <th>Hours</th>
                  <th>Slot Duration</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let a of availabilities">
                  <td><strong>{{ a.dayOfWeek }}</strong></td>
                  <td>{{ a.startTime }} – {{ a.endTime }}</td>
                  <td>{{ a.slotDurationMinutes }} mins</td>
                  <td>
                    <span class="badge" [ngClass]="a.active ? 'badge-success' : 'badge-neutral'">
                      {{ a.active ? 'Active' : 'Inactive' }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <ng-template #noSched>
            <div class="empty-state">
              <p>No availability configured yet. Use the form to add working hours.</p>
            </div>
          </ng-template>
        </div>

        <!-- Add / Update Hours Form -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">Set Hours for Day</span>
          </div>

          <form [formGroup]="scheduleForm" (ngSubmit)="onSubmit()">
            <div class="form-group">
              <label class="form-label">Day of the Week *</label>
              <select class="form-select" formControlName="dayOfWeek">
                <option value="MONDAY">Monday</option>
                <option value="TUESDAY">Tuesday</option>
                <option value="WEDNESDAY">Wednesday</option>
                <option value="THURSDAY">Thursday</option>
                <option value="FRIDAY">Friday</option>
                <option value="SATURDAY">Saturday</option>
                <option value="SUNDAY">Sunday</option>
              </select>
            </div>

            <div class="grid-2">
              <div class="form-group">
                <label class="form-label">Start Time *</label>
                <input type="time" class="form-control" formControlName="startTime">
              </div>
              <div class="form-group">
                <label class="form-label">End Time *</label>
                <input type="time" class="form-control" formControlName="endTime">
              </div>
            </div>

            <div class="grid-2">
              <div class="form-group">
                <label class="form-label">Slot Duration (Minutes)</label>
                <select class="form-select" formControlName="slotDurationMinutes">
                  <option [value]="15">15 Minutes</option>
                  <option [value]="30">30 Minutes (Standard)</option>
                  <option [value]="45">45 Minutes</option>
                  <option [value]="60">60 Minutes</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">Practice Day Status</label>
                <select class="form-select" formControlName="active">
                  <option [value]="true">Active Practice Day</option>
                  <option [value]="false">Off / Inactive</option>
                </select>
              </div>
            </div>

            <button type="submit" class="btn btn-primary btn-block" [disabled]="scheduleForm.invalid || saving">
              <span *ngIf="!saving">Save Working Hours</span>
              <span *ngIf="saving">Saving...</span>
            </button>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .btn-block {
      width: 100%;
      margin-top: 1rem;
    }
  `]
})
export class DoctorScheduleComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private doctorService = inject(DoctorService);
  private toastService = inject(ToastService);

  availabilities: DoctorAvailability[] = [];
  saving = false;

  scheduleForm = this.fb.group({
    dayOfWeek: ['MONDAY', Validators.required],
    startTime: ['09:00', Validators.required],
    endTime: ['17:00', Validators.required],
    slotDurationMinutes: [30, Validators.required],
    active: [true, Validators.required]
  });

  ngOnInit(): void {
    this.loadSchedule();
  }

  loadSchedule(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.doctorService.getDoctorAvailability(profileId).subscribe({
        next: (data) => (this.availabilities = data)
      });
    }
  }

  onSubmit(): void {
    if (this.scheduleForm.invalid) return;

    const profileId = this.authService.profileId;
    if (!profileId) return;

    this.saving = true;
    const val = this.scheduleForm.getRawValue();

    this.doctorService.setDoctorAvailability(profileId, {
      dayOfWeek: val.dayOfWeek as any,
      startTime: val.startTime!,
      endTime: val.endTime!,
      slotDurationMinutes: Number(val.slotDurationMinutes),
      active: val.active === true || val.active === 'true' as any
    }).subscribe({
      next: () => {
        this.saving = false;
        this.toastService.success(`Schedule updated for ${val.dayOfWeek}`);
        this.loadSchedule();
      },
      error: () => {
        this.saving = false;
      }
    });
  }
}
