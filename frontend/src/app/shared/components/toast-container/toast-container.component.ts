import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, Toast } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container">
      <div *ngFor="let toast of toastService.toasts$ | async" 
           class="toast toast-{{ toast.type }}" 
           (click)="toastService.remove(toast.id)">
        <div class="toast-header" *ngIf="toast.title">
          <strong>{{ toast.title }}</strong>
          <span class="toast-close">&times;</span>
        </div>
        <div class="toast-body">{{ toast.message }}</div>
      </div>
    </div>
  `,
  styles: [`
    .toast-container {
      position: fixed;
      top: 1.5rem;
      right: 1.5rem;
      z-index: 9999;
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
      max-width: 380px;
      width: 100%;
      pointer-events: none;
    }
    .toast {
      pointer-events: auto;
      padding: 1rem;
      border-radius: var(--radius-sm);
      box-shadow: var(--shadow-lg);
      background: #ffffff;
      border-left: 4px solid #94a3b8;
      cursor: pointer;
      animation: slideIn 0.2s ease-out;
    }
    .toast-success { border-left-color: var(--success); background: #f0fdf4; }
    .toast-danger { border-left-color: var(--danger); background: #fef2f2; }
    .toast-warning { border-left-color: var(--warning); background: #fffbeb; }
    .toast-info { border-left-color: var(--primary); background: #f0f9ff; }
    .toast-header {
      display: flex;
      justify-content: space-between;
      font-size: 0.875rem;
      margin-bottom: 0.25rem;
    }
    .toast-body {
      font-size: 0.8125rem;
      color: var(--text-main);
    }
    .toast-close {
      font-weight: bold;
      opacity: 0.5;
    }
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
  `]
})
export class ToastContainerComponent {
  toastService = inject(ToastService);
}
