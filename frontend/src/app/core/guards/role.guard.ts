import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../../models/auth.models';

export const roleGuard: (expectedRoles: Role[]) => CanActivateFn = (expectedRoles: Role[]) => {
  return () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated) {
      router.navigate(['/login']);
      return false;
    }

    const currentRole = authService.role;
    if (currentRole && expectedRoles.includes(currentRole)) {
      return true;
    }

    // Role mismatch, redirect to user's appropriate default dashboard
    if (currentRole === 'ROLE_ADMIN') {
      router.navigate(['/admin/dashboard']);
    } else if (currentRole === 'ROLE_DOCTOR') {
      router.navigate(['/doctor/dashboard']);
    } else if (currentRole === 'ROLE_PATIENT') {
      router.navigate(['/patient/dashboard']);
    } else {
      router.navigate(['/login']);
    }

    return false;
  };
};
