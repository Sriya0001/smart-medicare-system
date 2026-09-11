import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="stat-card">
      <div class="stat-icon" *ngIf="icon">
        <span>{{ icon }}</span>
      </div>
      <div class="stat-info">
        <div class="stat-label">{{ label }}</div>
        <div class="stat-value">{{ value }}</div>
        <div class="stat-sub" *ngIf="subtitle">{{ subtitle }}</div>
      </div>
    </div>
  `,
  styles: [`
    .stat-card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: var(--radius-md);
      padding: 1.25rem 1.5rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      box-shadow: var(--shadow-sm);
    }
    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-sm);
      background-color: var(--primary-light);
      color: var(--primary);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      flex-shrink: 0;
    }
    .stat-info {
      flex: 1;
    }
    .stat-label {
      font-size: 0.8125rem;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .stat-value {
      font-size: 1.75rem;
      font-weight: 700;
      color: var(--secondary);
      line-height: 1.2;
      margin: 0.25rem 0;
    }
    .stat-sub {
      font-size: 0.75rem;
      color: var(--accent);
      font-weight: 500;
    }
  `]
})
export class StatCardComponent {
  @Input() label: string = '';
  @Input() value: string | number = 0;
  @Input() subtitle?: string;
  @Input() icon?: string;
}
