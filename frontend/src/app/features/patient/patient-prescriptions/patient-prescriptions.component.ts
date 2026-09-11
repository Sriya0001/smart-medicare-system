import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { Prescription } from '../../../models/consultation.models';

@Component({
  selector: 'app-patient-prescriptions',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">My Prescriptions</h1>
          <p class="page-subtitle">View and print all e-prescriptions issued by your attending physicians</p>
        </div>
      </div>

      <div *ngIf="prescriptions && prescriptions.length > 0; else emptyRx">
        <div class="card rx-full-card" *ngFor="let rx of prescriptions">
          <div class="rx-card-header">
            <div>
              <div class="rx-id">Prescription #{{ rx.id }}</div>
              <div class="rx-doc">Prescribed by <strong>{{ rx.doctorName }}</strong></div>
            </div>
            <div class="rx-date-box">
              <span class="rx-issue-label">Issue Date:</span>
              <span class="rx-issue-val">{{ rx.issueDate }}</span>
            </div>
          </div>

          <div class="table-responsive">
            <table class="table">
              <thead>
                <tr>
                  <th>Medication</th>
                  <th>Dosage</th>
                  <th>Frequency</th>
                  <th>Duration</th>
                  <th>Instructions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of rx.items">
                  <td><strong>{{ item.medicationName }}</strong></td>
                  <td>{{ item.dosage }}</td>
                  <td>{{ item.frequency }}</td>
                  <td>{{ item.duration }}</td>
                  <td>{{ item.instructions || 'As directed by physician' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="rx-notes" *ngIf="rx.notes">
            <strong>Physician Directives:</strong> {{ rx.notes }}
          </div>
        </div>
      </div>

      <ng-template #emptyRx>
        <div class="card">
          <div class="empty-state">
            <div class="empty-state-icon">💊</div>
            <div class="empty-state-title">No Prescriptions On File</div>
            <p>Your electronic prescriptions will appear here after consultations.</p>
          </div>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .rx-full-card {
      margin-bottom: 2rem;
      border-top: 4px solid var(--primary);
    }
    .rx-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--border);
    }
    .rx-id {
      font-size: 1.125rem;
      font-weight: 700;
      color: var(--secondary);
    }
    .rx-doc {
      font-size: 0.8125rem;
      color: var(--text-muted);
    }
    .rx-date-box {
      text-align: right;
    }
    .rx-issue-label {
      font-size: 0.75rem;
      color: var(--text-sub);
      display: block;
    }
    .rx-issue-val {
      font-weight: 600;
      font-size: 0.875rem;
      color: var(--secondary);
    }
    .rx-notes {
      margin-top: 1rem;
      padding: 0.75rem;
      background-color: #f8fafc;
      border-radius: var(--radius-sm);
      font-size: 0.8125rem;
      color: var(--text-main);
    }
  `]
})
export class PatientPrescriptionsComponent implements OnInit {
  private authService = inject(AuthService);
  private patientService = inject(PatientService);

  prescriptions: Prescription[] = [];

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.patientService.getPatientPrescriptions(profileId).subscribe({
        next: (data) => (this.prescriptions = data)
      });
    }
  }
}
