import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-wrapper">
      <div class="auth-card">
        <div class="auth-header">
          <div class="auth-logo">⚕ MediCare</div>
          <h1 class="auth-title">Create Patient Account</h1>
          <p class="auth-subtitle">Join the patient portal to book and manage appointments</p>
        </div>

        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
          <div class="grid-2">
            <div class="form-group">
              <label class="form-label">First Name *</label>
              <input type="text" class="form-control" formControlName="firstName" placeholder="e.g. Alice">
            </div>
            <div class="form-group">
              <label class="form-label">Last Name *</label>
              <input type="text" class="form-control" formControlName="lastName" placeholder="e.g. Walker">
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Email Address *</label>
            <input type="email" class="form-control" formControlName="email" placeholder="alice.walker@example.com">
          </div>

          <div class="form-group">
            <label class="form-label">Password *</label>
            <input type="password" class="form-control" formControlName="password" placeholder="At least 6 characters">
          </div>

          <div class="grid-2">
            <div class="form-group">
              <label class="form-label">Phone Number</label>
              <input type="tel" class="form-control" formControlName="phoneNumber" placeholder="+1 (555) 000-0000">
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
            <label class="form-label">Address</label>
            <input type="text" class="form-control" formControlName="address" placeholder="Residential Street Address">
          </div>

          <button type="submit" class="btn btn-primary btn-block" [disabled]="registerForm.invalid || loading">
            <span *ngIf="!loading">Create Account</span>
            <span *ngIf="loading">Creating Account...</span>
          </button>
        </form>

        <div class="auth-footer">
          Already registered? <a routerLink="/login">Sign In Here</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-wrapper {
      min-height: calc(100vh - 64px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 2rem 1rem;
      background: linear-gradient(135deg, #f8fafc 0%, #e0f2fe 100%);
    }
    .auth-card {
      background: #ffffff;
      border: 1px solid var(--border);
      border-radius: var(--radius-lg);
      box-shadow: var(--shadow-lg);
      max-width: 560px;
      width: 100%;
      padding: 2.5rem 2rem;
    }
    .auth-header {
      text-align: center;
      margin-bottom: 2rem;
    }
    .auth-logo {
      font-size: 1.75rem;
      font-weight: 800;
      color: var(--primary);
      margin-bottom: 0.5rem;
    }
    .auth-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--secondary);
    }
    .auth-subtitle {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
    }
    .btn-block {
      width: 100%;
      margin-top: 0.5rem;
    }
    .auth-footer {
      text-align: center;
      margin-top: 1.5rem;
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    .auth-footer a {
      color: var(--primary);
      font-weight: 600;
      text-decoration: none;
    }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  loading = false;

  registerForm = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    phoneNumber: [''],
    dateOfBirth: [''],
    gender: [''],
    bloodGroup: [''],
    address: ['']
  });

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.loading = true;
    const val = this.registerForm.getRawValue();

    this.authService.register({
      firstName: val.firstName!,
      lastName: val.lastName!,
      email: val.email!,
      password: val.password!,
      role: 'ROLE_PATIENT',
      phoneNumber: val.phoneNumber || undefined,
      dateOfBirth: val.dateOfBirth || undefined,
      gender: val.gender || undefined,
      bloodGroup: val.bloodGroup || undefined,
      address: val.address || undefined
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.toastService.success(`Account registered! Welcome to MediCare.`);
        this.router.navigate(['/patient/dashboard']);
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
