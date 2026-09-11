import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Public Routes
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent)
  },

  // Patient Routes
  {
    path: 'patient',
    canActivate: [authGuard, roleGuard(['ROLE_PATIENT', 'ROLE_ADMIN'])],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/patient/patient-dashboard/patient-dashboard.component').then(m => m.PatientDashboardComponent)
      },
      {
        path: 'doctors',
        loadComponent: () => import('./features/patient/doctor-directory/doctor-directory.component').then(m => m.DoctorDirectoryComponent)
      },
      {
        path: 'book-appointment',
        loadComponent: () => import('./features/patient/book-appointment/book-appointment.component').then(m => m.BookAppointmentComponent)
      },
      {
        path: 'appointments',
        loadComponent: () => import('./features/patient/my-appointments/my-appointments.component').then(m => m.MyAppointmentsComponent)
      },
      {
        path: 'medical-history',
        loadComponent: () => import('./features/patient/patient-history/patient-history.component').then(m => m.PatientHistoryComponent)
      },
      {
        path: 'prescriptions',
        loadComponent: () => import('./features/patient/patient-prescriptions/patient-prescriptions.component').then(m => m.PatientPrescriptionsComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/patient/patient-profile/patient-profile.component').then(m => m.PatientProfileComponent)
      }
    ]
  },

  // Doctor Routes
  {
    path: 'doctor',
    canActivate: [authGuard, roleGuard(['ROLE_DOCTOR', 'ROLE_ADMIN'])],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/doctor/doctor-dashboard/doctor-dashboard.component').then(m => m.DoctorDashboardComponent)
      },
      {
        path: 'appointments',
        loadComponent: () => import('./features/doctor/doctor-appointments/doctor-appointments.component').then(m => m.DoctorAppointmentsComponent)
      },
      {
        path: 'schedule',
        loadComponent: () => import('./features/doctor/doctor-schedule/doctor-schedule.component').then(m => m.DoctorScheduleComponent)
      },
      {
        path: 'patients',
        loadComponent: () => import('./features/doctor/doctor-patients/doctor-patients.component').then(m => m.DoctorPatientsComponent)
      },
      {
        path: 'consultation/:appointmentId',
        loadComponent: () => import('./features/doctor/doctor-consultation/doctor-consultation.component').then(m => m.DoctorConsultationComponent)
      },
      {
        path: 'prescriptions',
        loadComponent: () => import('./features/doctor/doctor-prescriptions/doctor-prescriptions.component').then(m => m.DoctorPrescriptionsComponent)
      }
    ]
  },

  // Admin Routes
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])],
    children: [
      {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
      },
      {
        path: 'dashboard',
        loadComponent: () => import('./features/admin/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent)
      },
      {
        path: 'doctors',
        loadComponent: () => import('./features/admin/doctor-management/doctor-management.component').then(m => m.DoctorManagementComponent)
      },
      {
        path: 'patients',
        loadComponent: () => import('./features/admin/patient-management/patient-management.component').then(m => m.PatientManagementComponent)
      },
      {
        path: 'appointments',
        loadComponent: () => import('./features/admin/appointment-management/appointment-management.component').then(m => m.AppointmentManagementComponent)
      }
    ]
  },

  // Fallback
  {
    path: '**',
    redirectTo: 'login'
  }
];
