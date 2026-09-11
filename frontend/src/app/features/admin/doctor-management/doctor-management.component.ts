import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { DoctorService } from '../../../core/services/doctor.service';
import { ToastService } from '../../../core/services/toast.service';
import { Doctor } from '../../../models/doctor.models';

@Component({
  selector: 'app-doctor-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Doctor Management & Credentials</h1>
          <p class="page-subtitle">Add new physicians, update medical credentials, and manage active status</p>
        </div>
        <button class="btn btn-primary" (click)="openAddModal()">+ Add New Doctor</button>
      </div>

      <div class="card">
        <div class="table-responsive" *ngIf="doctors && doctors.length > 0; else emptyDocs">
          <table class="table">
            <thead>
              <tr>
                <th>Doctor</th>
                <th>Specialty</th>
                <th>License #</th>
                <th>Phone</th>
                <th>Experience</th>
                <th>Fee</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let doc of doctors">
                <td>
                  <strong>{{ doc.fullName }}</strong>
                  <div class="email-sub">{{ doc.email }}</div>
                </td>
                <td><span class="badge badge-primary">{{ doc.specialty }}</span></td>
                <td>{{ doc.licenseNumber }}</td>
                <td>{{ doc.phoneNumber || 'N/A' }}</td>
                <td>{{ doc.experienceYears || 0 }} Yrs</td>
                <td>\${{ doc.consultationFee || 0 }}</td>
                <td>
                  <span class="badge" [ngClass]="doc.active ? 'badge-success' : 'badge-danger'">
                    {{ doc.active ? 'Active' : 'Inactive' }}
                  </span>
                </td>
                <td>
                  <button *ngIf="doc.active" class="btn btn-danger btn-sm" (click)="deactivateDoctor(doc.id)">
                    Deactivate
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #emptyDocs>
          <div class="empty-state">
            <p>No doctors registered in the practice yet.</p>
          </div>
        </ng-template>
      </div>

      <!-- Add Doctor Modal -->
      <div class="modal-backdrop" *ngIf="showModal" (click)="closeModal()">
        <div class="modal-dialog" (click)="$event.stopPropagation()">
          <div class="card-header">
            <span class="card-title">Add New Practice Doctor</span>
            <button class="btn btn-outline btn-sm" (click)="closeModal()">&times;</button>
          </div>

          <form [formGroup]="doctorForm" (ngSubmit)="onSubmit()">
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
                <label class="form-label">Email Address *</label>
                <input type="email" class="form-control" formControlName="email">
              </div>
              <div class="form-group">
                <label class="form-label">Temporary Password *</label>
                <input type="password" class="form-control" formControlName="password" placeholder="At least 6 characters">
              </div>
            </div>

            <div class="grid-2">
              <div class="form-group">
                <label class="form-label">Specialty *</label>
                <input type="text" class="form-control" formControlName="specialty" placeholder="e.g. Cardiology, Pediatrics">
              </div>
              <div class="form-group">
                <label class="form-label">License Number *</label>
                <input type="text" class="form-control" formControlName="licenseNumber" placeholder="e.g. LIC-MD-12345">
              </div>
            </div>

            <div class="grid-2">
              <div class="form-group">
                <label class="form-label">Phone Number</label>
                <input type="tel" class="form-control" formControlName="phoneNumber">
              </div>
              <div class="form-group">
                <label class="form-label">Experience (Years)</label>
                <input type="number" class="form-control" formControlName="experienceYears">
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Consultation Fee ($)</label>
              <input type="number" class="form-control" formControlName="consultationFee">
            </div>

            <div class="form-group">
              <label class="form-label">Biography</label>
              <textarea class="form-control" formControlName="biography" placeholder="Short professional background..."></textarea>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1rem;">
              <button type="button" class="btn btn-secondary" (click)="closeModal()">Cancel</button>
              <button type="submit" class="btn btn-primary" [disabled]="doctorForm.invalid || submitting">
                <span *ngIf="!submitting">Create Doctor Profile</span>
                <span *ngIf="submitting">Creating...</span>
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .email-sub {
      font-size: 0.75rem;
      color: var(--text-muted);
    }
  `]
})
export class DoctorManagementComponent implements OnInit {
  private fb = inject(FormBuilder);
  private doctorService = inject(DoctorService);
  private toastService = inject(ToastService);

  doctors: Doctor[] = [];
  showModal = false;
  submitting = false;

  doctorForm = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    specialty: ['', Validators.required],
    licenseNumber: ['', Validators.required],
    phoneNumber: [''],
    experienceYears: [5],
    consultationFee: [100],
    biography: ['']
  });

  ngOnInit(): void {
    this.loadDoctors();
  }

  loadDoctors(): void {
    this.doctorService.getAllDoctorsForAdmin().subscribe({
      next: (data) => (this.doctors = data)
    });
  }

  openAddModal(): void {
    this.doctorForm.reset({ experienceYears: 5, consultationFee: 100 });
    this.showModal = true;
  }

  closeModal(): void {
    this.showModal = false;
  }

  onSubmit(): void {
    if (this.doctorForm.invalid) return;

    this.submitting = true;
    const val = this.doctorForm.getRawValue();

    this.doctorService.createDoctor({
      firstName: val.firstName!,
      lastName: val.lastName!,
      email: val.email!,
      password: val.password!,
      specialty: val.specialty!,
      licenseNumber: val.licenseNumber!,
      phoneNumber: val.phoneNumber || undefined,
      experienceYears: val.experienceYears || 5,
      consultationFee: val.consultationFee || 100,
      biography: val.biography || undefined
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.toastService.success('Doctor created successfully with default Mon-Fri practice hours.');
        this.closeModal();
        this.loadDoctors();
      },
      error: () => {
        this.submitting = false;
      }
    });
  }

  deactivateDoctor(id: number): void {
    if (confirm('Deactivate this doctor? They will no longer accept new appointments.')) {
      this.doctorService.deactivateDoctor(id).subscribe({
        next: () => {
          this.toastService.info('Doctor deactivated.');
          this.loadDoctors();
        }
      });
    }
  }
}
