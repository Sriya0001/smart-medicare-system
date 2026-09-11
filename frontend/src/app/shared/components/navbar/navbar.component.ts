import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header class="navbar">
      <div class="navbar-brand">
        <a routerLink="/" class="brand-link">
          <span class="brand-badge">⚕</span>
          <span class="brand-name">MediCare</span>
          <span class="brand-sub">Practice Management</span>
        </a>
      </div>

      <div class="navbar-actions" *ngIf="authService.currentUser$ | async as user; else anonNav">
        <div class="user-badge">
          <span class="user-avatar">{{ user.fullName.charAt(0) }}</span>
          <div class="user-details">
            <span class="user-name">{{ user.fullName }}</span>
            <span class="user-role">{{ formatRole(user.role) }}</span>
          </div>
        </div>
        <button class="btn btn-outline btn-sm" (click)="authService.logout()">
          Logout
        </button>
      </div>

      <ng-template #anonNav>
        <div class="navbar-actions">
          <a routerLink="/login" class="btn btn-outline btn-sm">Sign In</a>
          <a routerLink="/register" class="btn btn-primary btn-sm">Get Started</a>
        </div>
      </ng-template>
    </header>
  `,
  styles: [`
    .navbar {
      height: 64px;
      background-color: #ffffff;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 1.5rem;
      position: sticky;
      top: 0;
      z-index: 50;
    }
    .brand-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      text-decoration: none;
      color: inherit;
    }
    .brand-badge {
      font-size: 1.5rem;
      color: var(--primary);
    }
    .brand-name {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--secondary);
      letter-spacing: -0.02em;
    }
    .brand-sub {
      font-size: 0.75rem;
      font-weight: 500;
      color: var(--text-muted);
      border-left: 1px solid var(--border);
      padding-left: 0.5rem;
      margin-left: 0.25rem;
    }
    .navbar-actions {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .user-badge {
      display: flex;
      align-items: center;
      gap: 0.625rem;
    }
    .user-avatar {
      width: 34px;
      height: 34px;
      border-radius: 50%;
      background-color: var(--primary-light);
      color: var(--primary);
      font-weight: 700;
      font-size: 0.875rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .user-details {
      display: flex;
      flex-direction: column;
      line-height: 1.2;
    }
    .user-name {
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--secondary);
    }
    .user-role {
      font-size: 0.6875rem;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
  `]
})
export class NavbarComponent {
  authService = inject(AuthService);

  formatRole(role: string): string {
    return role ? role.replace('ROLE_', '') : '';
  }
}
