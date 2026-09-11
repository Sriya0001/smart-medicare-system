import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { Patient } from '../../../models/patient.models';

@Component({
  selector: 'app-doctor-patients',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Patient Panel</h1>
          <p class="page-subtitle">Roster of all patients who have clinical appointments with you</p>
        </div>
      </div>

      <div class="card">
        <div class="table-responsive" *ngIf="patients && patients.length > 0; else noPatients">
          <table class="table">
            <thead>
              <tr>
                <th>Patient Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>DOB / Age</th>
                <th>Blood Group</th>
                <th>Emergency Contact</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let p of patients">
                <td><strong>{{ p.fullName }}</strong></td>
                <td>{{ p.email }}</td>
                <td>{{ p.phoneNumber || 'N/A' }}</td>
                <td>{{ p.dateOfBirth || 'N/A' }}</td>
                <td><span class="badge badge-primary">{{ p.bloodGroup || 'Unknown' }}</span></td>
                <td>{{ p.emergencyContact || 'None listed' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noPatients>
          <div class="empty-state">
            <div class="empty-state-icon">👥</div>
            <div class="empty-state-title">No Patients in Roster</div>
            <p>Patients who book appointments with you will appear in your panel.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `
})
export class DoctorPatientsComponent implements OnInit {
  private authService = inject(AuthService);
  private doctorService = inject(DoctorService);

  patients: Patient[] = [];

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.doctorService.getDoctorPatients(profileId).subscribe({
        next: (data) => (this.patients = data)
      });
    }
  }
}
