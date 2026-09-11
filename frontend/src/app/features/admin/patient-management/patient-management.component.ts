import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PatientService } from '../../../core/services/patient.service';
import { Patient } from '../../../models/patient.models';

@Component({
  selector: 'app-patient-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Patient Master Registry</h1>
          <p class="page-subtitle">Search, view, and inspect all registered practice patients</p>
        </div>
      </div>

      <div class="card search-card">
        <input type="text" class="form-control" [(ngModel)]="searchQuery" (input)="onSearch()" placeholder="Search patients by name, email, phone...">
      </div>

      <div class="card">
        <div class="table-responsive" *ngIf="patients && patients.length > 0; else noPatients">
          <table class="table">
            <thead>
              <tr>
                <th>Patient ID</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>DOB</th>
                <th>Gender</th>
                <th>Blood Group</th>
                <th>Registered</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let p of patients">
                <td><strong>#{{ p.id }}</strong></td>
                <td><strong>{{ p.fullName }}</strong></td>
                <td>{{ p.email }}</td>
                <td>{{ p.phoneNumber || 'N/A' }}</td>
                <td>{{ p.dateOfBirth || 'N/A' }}</td>
                <td>{{ p.gender || 'N/A' }}</td>
                <td><span class="badge badge-primary">{{ p.bloodGroup || '—' }}</span></td>
                <td>{{ p.createdAt | date:'mediumDate' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noPatients>
          <div class="empty-state">
            <p>No matching patient records found.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .search-card {
      padding: 1rem;
      margin-bottom: 1.5rem;
    }
  `]
})
export class PatientManagementComponent implements OnInit {
  private patientService = inject(PatientService);

  patients: Patient[] = [];
  searchQuery = '';

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.patientService.getAllPatients().subscribe({
      next: (data) => (this.patients = data)
    });
  }

  onSearch(): void {
    if (this.searchQuery.trim().length > 0) {
      this.patientService.searchPatients(this.searchQuery).subscribe({
        next: (data) => (this.patients = data)
      });
    } else {
      this.loadPatients();
    }
  }
}
