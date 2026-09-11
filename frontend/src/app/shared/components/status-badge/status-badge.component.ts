import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span [ngClass]="getBadgeClass()">
      {{ label || status }}
    </span>
  `,
  styles: [`
    span {
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.25rem 0.625rem;
      border-radius: 9999px;
      text-transform: capitalize;
      display: inline-block;
    }
  `]
})
export class StatusBadgeComponent {
  @Input() status: string = '';
  @Input() label?: string;

  getBadgeClass(): string {
    const s = this.status?.toUpperCase();
    switch (s) {
      case 'BOOKED':
        return 'badge badge-primary';
      case 'CONFIRMED':
        return 'badge badge-success';
      case 'COMPLETED':
        return 'badge badge-neutral';
      case 'CANCELLED':
        return 'badge badge-danger';
      case 'ACTIVE':
      case 'AVAILABLE':
        return 'badge badge-success';
      case 'INACTIVE':
      case 'PAST TIME':
        return 'badge badge-neutral';
      default:
        return 'badge badge-neutral';
    }
  }
}
