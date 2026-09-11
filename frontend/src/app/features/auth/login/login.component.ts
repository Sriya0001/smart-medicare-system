import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-wrapper">
      <div class="auth-card">
        <div class="auth-header">
          <div class="auth-logo">⚕ MediCare</div>
          <h1 class="auth-title">Welcome Back</h1>
          <p class="auth-subtitle">Sign in to your medical practice account</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <input type="email" class="form-control" formControlName="email" placeholder="e.g. sarah.jenkins@medicare.com">
          </div>

          <div class="form-group">
            <label class="form-label">Password</label>
            <input type="password" class="form-control" formControlName="password" placeholder="••••••••">
          </div>

          <button type="submit" class="btn btn-primary btn-block" [disabled]="loginForm.invalid || loading">
            <span *ngIf="!loading">Sign In</span>
            <span *ngIf="loading">Signing In...</span>
          </button>
        </form>

        <div class="demo-box">
          <div class="demo-title">Demo Credentials:</div>
          <div class="demo-item" (click)="fillCreds('admin@medicare.com', 'Admin@123')">
            <strong>Admin:</strong> admin&#64;medicare.com / Admin&#64;123
          </div>
          <div class="demo-item" (click)="fillCreds('sarah.jenkins@medicare.com', 'Doctor@123')">
            <strong>Doctor:</strong> sarah.jenkins&#64;medicare.com / Doctor&#64;123
          </div>
          <div class="demo-item" (click)="fillCreds('john.smith@gmail.com', 'Patient@123')">
            <strong>Patient:</strong> john.smith&#64;gmail.com / Patient&#64;123
          </div>
        </div>

        <div class="auth-footer">
          Don't have an account? <a routerLink="/register">Create Patient Account</a>
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
      max-width: 440px;
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
    .demo-box {
      margin-top: 1.5rem;
      padding: 0.875rem;
      background-color: #f8fafc;
      border: 1px dashed var(--border);
      border-radius: var(--radius-sm);
      font-size: 0.75rem;
    }
    .demo-title {
      font-weight: 700;
      color: var(--secondary);
      margin-bottom: 0.375rem;
    }
    .demo-item {
      cursor: pointer;
      color: var(--primary);
      margin-bottom: 0.25rem;
      padding: 0.125rem 0.25rem;
      border-radius: 4px;
    }
    .demo-item:hover {
      background-color: var(--primary-light);
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
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toastService = inject(ToastService);

  loading = false;

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]]
  });

  fillCreds(email: string, pass: string): void {
    this.loginForm.patchValue({ email, password: pass });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    const val = this.loginForm.getRawValue();

    this.authService.login({ email: val.email!, password: val.password! }).subscribe({
      next: (res) => {
        this.loading = false;
        this.toastService.success(`Welcome back, ${res.fullName}!`);

        const returnUrl = this.route.snapshot.queryParams['returnUrl'];
        if (returnUrl) {
          this.router.navigateByUrl(returnUrl);
          return;
        }

        if (res.role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else if (res.role === 'ROLE_DOCTOR') {
          this.router.navigate(['/doctor/dashboard']);
        } else {
          this.router.navigate(['/patient/dashboard']);
        }
      },
      error: () => {
        this.loading = false;
      }
    });
  }
}
