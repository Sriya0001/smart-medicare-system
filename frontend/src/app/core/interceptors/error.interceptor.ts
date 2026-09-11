import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred.';

      if (error.error && typeof error.error === 'object') {
        if (error.error.message) {
          errorMessage = error.error.message;
        } else if (error.error.validationErrors) {
          const firstKey = Object.keys(error.error.validationErrors)[0];
          errorMessage = error.error.validationErrors[firstKey];
        }
      } else if (typeof error.error === 'string') {
        errorMessage = error.error;
      }

      if (error.status === 401) {
        toastService.error('Session expired or unauthorized. Please log in.', 'Unauthorized');
        authService.logout();
      } else if (error.status === 403) {
        toastService.error('You do not have permission to perform this action.', 'Forbidden');
      } else if (error.status === 409) {
        toastService.warning(errorMessage, 'Scheduling Conflict');
      } else if (error.status === 400) {
        toastService.error(errorMessage, 'Bad Request');
      } else if (error.status === 404) {
        toastService.error(errorMessage, 'Not Found');
      } else if (error.status >= 500) {
        toastService.error(errorMessage, 'Server Error');
      }

      return throwError(() => error);
    })
  );
};
