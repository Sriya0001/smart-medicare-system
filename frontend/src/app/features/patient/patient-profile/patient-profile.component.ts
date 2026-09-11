import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { ToastService } from '../../../core/services/toast.service';
import { Patient } from '../../../models/patient.models';

@Component({
  selector: 'app-patient-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Personal Patient Profile</h1>
          <p class="page-subtitle">Manage your personal demographics, emergency contact, and clinical metadata</p>
        </div>
      </div>

      <div class="card profile-card" *ngIf="patient">
        <form [formGroup]="profileForm" (ngSubmit)="onSubmit()">
          <div class="grid-2">
            <div class="form-group">
              <label class="form-label">First Name *</label>
              <input type="text" class="form-control" formControlName="firstName">
            </div>
            <div class="form-group">
              <label class="form-label">Last Name *</label>
              <input type="text" class="form-control" formControlName="lastName">
            </div>
          </div>

          <div class="grid-2">
            <div class="form-group">
              <label class="form-label">Phone Number</label>
              <input type="tel" class="form-control" formControlName="phoneNumber">
            </div>
            <div class="form-group">
              <label class="form-label">Date of Birth</label>
              <input type="date" class="form-control" formControlName="dateOfBirth">
            </div>
          </div>

          <div class="grid-2">
            <div class="form-group">
              <label class="form-label">Gender</label>
              <select class="form-select" formControlName="gender">
                <option value="">Select Gender</option>
                <option value="Male">Male</option>
                <option value="Female">Female</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">Blood Group</label>
              <select class="form-select" formControlName="bloodGroup">
                <option value="">Select Blood Group</option>
                <option value="A+">A+</option>
                <option value="A-">A-</option>
                <option value="B+">B+</option>
                <option value="B-">B-</option>
                <option value="O+">O+</option>
                <option value="O-">O-</option>
                <option value="AB+">AB+</option>
                <option value="AB-">AB-</option>
              </select>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Residential Address</label>
            <input type="text" class="form-control" formControlName="address">
          </div>

          <div class="form-group">
            <label class="form-label">Emergency Contact Name & Phone</label>
            <input type="text" class="form-control" formControlName="emergencyContact" placeholder="e.g. John Doe (Spouse) +1 555-0199">
          </div>

          <div class="form-actions">
            <button type="submit" class="btn btn-primary" [disabled]="profileForm.invalid || saving">
              <span *ngIf="!saving">Save Changes</span>
              <span *ngIf="saving">Saving...</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .profile-card {
      max-width: 720px;
    }
    .form-actions {
      margin-top: 1.5rem;
      padding-top: 1rem;
      border-top: 1px solid var(--border);
      display: flex;
      justify-content: flex-end;
    }
  `]
})
export class PatientProfileComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private patientService = inject(PatientService);
  private toastService = inject(ToastService);

  patient: Patient | null = null;
  saving = false;

  profileForm = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    phoneNumber: [''],
    dateOfBirth: [''],
    gender: [''],
    bloodGroup: [''],
    address: [''],
    emergencyContact: ['']
  });

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.patientService.getPatientById(profileId).subscribe({
        next: (data) => {
          this.patient = data;
          this.profileForm.patchValue({
            firstName: data.firstName,
            lastName: data.lastName,
            phoneNumber: data.phoneNumber || '',
            dateOfBirth: data.dateOfBirth || '',
            gender: data.gender || '',
            bloodGroup: data.bloodGroup || '',
            address: data.address || '',
            emergencyContact: data.emergencyContact || ''
          });
        }
      });
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid || !this.patient) return;

    this.saving = true;
    const val = this.profileForm.getRawValue();

    this.patientService.updatePatient(this.patient.id, {
      firstName: val.firstName!,
      lastName: val.lastName!,
      phoneNumber: val.phoneNumber || undefined,
      dateOfBirth: val.dateOfBirth || undefined,
      gender: val.gender || undefined,
      bloodGroup: val.bloodGroup || undefined,
      address: val.address || undefined,
      emergencyContact: val.emergencyContact || undefined
    }).subscribe({
      next: (updated) => {
        this.saving = false;
        this.patient = updated;
        this.authService.updateStoredUser({ fullName: updated.fullName });
        this.toastService.success('Profile updated successfully.');
      },
      error: () => {
        this.saving = false;
      }
    });
  }
}
