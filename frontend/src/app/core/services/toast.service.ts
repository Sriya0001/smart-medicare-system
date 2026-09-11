import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface Toast {
  id: string;
  type: 'success' | 'danger' | 'warning' | 'info';
  title?: string;
  message: string;
  duration?: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private toastsSubject = new BehaviorSubject<Toast[]>([]);
  toasts$ = this.toastsSubject.asObservable();

  show(toast: Omit<Toast, 'id'>): void {
    const id = Math.random().toString(36).substring(2, 9);
    const newToast: Toast = { ...toast, id, duration: toast.duration || 4000 };
    const current = this.toastsSubject.value;
    this.toastsSubject.next([...current, newToast]);

    setTimeout(() => {
      this.remove(id);
    }, newToast.duration);
  }

  success(message: string, title: string = 'Success'): void {
    this.show({ type: 'success', title, message });
  }

  error(message: string, title: string = 'Error'): void {
    this.show({ type: 'danger', title, message, duration: 6000 });
  }

  info(message: string, title: string = 'Information'): void {
    this.show({ type: 'info', title, message });
  }

  warning(message: string, title: string = 'Warning'): void {
    this.show({ type: 'warning', title, message });
  }

  remove(id: string): void {
    const current = this.toastsSubject.value.filter(t => t.id !== id);
    this.toastsSubject.next(current);
  }
}
