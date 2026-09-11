import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar" *ngIf="authService.currentUser$ | async as user">
      <div class="nav-section">
        <span class="section-title">Navigation</span>

        <!-- Patient Navigation -->
        <ng-container *ngIf="user.role === 'ROLE_PATIENT'">
          <a routerLink="/patient/dashboard" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📊</span>
            <span>Dashboard</span>
          </a>
          <a routerLink="/patient/doctors" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">🩺</span>
            <span>Find Doctors</span>
          </a>
          <a routerLink="/patient/book-appointment" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📅</span>
            <span>Book Appointment</span>
          </a>
          <a routerLink="/patient/appointments" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📋</span>
            <span>My Appointments</span>
          </a>
          <a routerLink="/patient/medical-history" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📑</span>
            <span>Medical Records</span>
          </a>
          <a routerLink="/patient/prescriptions" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">💊</span>
            <span>Prescriptions</span>
          </a>
          <a routerLink="/patient/profile" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">👤</span>
            <span>My Profile</span>
          </a>
        </ng-container>

        <!-- Doctor Navigation -->
        <ng-container *ngIf="user.role === 'ROLE_DOCTOR'">
          <a routerLink="/doctor/dashboard" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📊</span>
            <span>Dashboard</span>
          </a>
          <a routerLink="/doctor/appointments" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📅</span>
            <span>Appointments</span>
          </a>
          <a routerLink="/doctor/schedule" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">⏱</span>
            <span>My Schedule</span>
          </a>
          <a routerLink="/doctor/patients" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">👥</span>
            <span>My Patients</span>
          </a>
          <a routerLink="/doctor/prescriptions" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">💊</span>
            <span>Prescriptions</span>
          </a>
        </ng-container>

        <!-- Admin Navigation -->
        <ng-container *ngIf="user.role === 'ROLE_ADMIN'">
          <a routerLink="/admin/dashboard" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📈</span>
            <span>Dashboard</span>
          </a>
          <a routerLink="/admin/doctors" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">🩺</span>
            <span>Doctor Directory</span>
          </a>
          <a routerLink="/admin/patients" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">👥</span>
            <span>Patient Registry</span>
          </a>
          <a routerLink="/admin/appointments" routerLinkActive="active" class="nav-link">
            <span class="nav-icon">📅</span>
            <span>All Appointments</span>
          </a>
        </ng-container>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar {
      width: 240px;
      background-color: #ffffff;
      border-right: 1px solid var(--border);
      padding: 1.5rem 1rem;
      display: flex;
      flex-direction: column;
      flex-shrink: 0;
    }
    .section-title {
      font-size: 0.6875rem;
      font-weight: 700;
      color: var(--text-sub);
      text-transform: uppercase;
      letter-spacing: 0.08em;
      padding: 0 0.75rem;
      margin-bottom: 0.5rem;
      display: block;
    }
    .nav-section {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.625rem 0.75rem;
      border-radius: var(--radius-sm);
      color: var(--text-main);
      text-decoration: none;
      font-size: 0.875rem;
      font-weight: 500;
      transition: all 0.15s ease;
    }
    .nav-link:hover {
      background-color: #f1f5f9;
      color: var(--primary-hover);
    }
    .nav-link.active {
      background-color: var(--primary-light);
      color: var(--primary);
      font-weight: 600;
    }
    .nav-icon {
      font-size: 1.125rem;
    }
  `]
})
export class SidebarComponent {
  authService = inject(AuthService);
}
