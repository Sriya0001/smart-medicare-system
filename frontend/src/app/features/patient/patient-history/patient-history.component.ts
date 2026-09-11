import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { Consultation } from '../../../models/consultation.models';

@Component({
  selector: 'app-patient-history',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Medical Records & Consultations</h1>
          <p class="page-subtitle">Complete chronological clinical encounter records and diagnoses</p>
        </div>
      </div>

      <div *ngIf="consultations && consultations.length > 0; else emptyHistory">
        <div class="card encounter-card" *ngFor="let cons of consultations">
          <div class="encounter-header">
            <div>
              <h2 class="diagnosis-title">{{ cons.diagnosis }}</h2>
              <div class="encounter-meta">
                <span>Attending: <strong>{{ cons.doctorName }}</strong> ({{ cons.doctorSpecialty }})</span>
                <span class="dot-sep">•</span>
                <span>Date: {{ cons.consultationDate | date:'fullDate' }}</span>
              </div>
            </div>
            <span class="badge badge-success">Finalized Record</span>
          </div>

          <div class="soap-grid">
            <div class="soap-section">
              <div class="soap-title">Chief Complaint & Symptoms</div>
              <p class="soap-text">{{ cons.symptoms }}</p>
            </div>

            <div class="soap-section" *ngIf="cons.treatmentPlan">
              <div class="soap-title">Treatment Plan & Directives</div>
              <p class="soap-text">{{ cons.treatmentPlan }}</p>
            </div>

            <div class="soap-section" *ngIf="cons.notes">
              <div class="soap-title">Clinical Examination Notes</div>
              <p class="soap-text">{{ cons.notes }}</p>
            </div>

            <div class="soap-section" *ngIf="cons.followUpDate">
              <div class="soap-title">Next Scheduled Follow-Up</div>
              <p class="soap-text follow-up">{{ cons.followUpDate | date:'mediumDate' }}</p>
            </div>
          </div>

          <!-- Attached Prescriptions -->
          <div class="prescriptions-box" *ngIf="cons.prescriptions && cons.prescriptions.length > 0">
            <div class="rx-header">Prescriptions Issued:</div>
            <div class="rx-card" *ngFor="let rx of cons.prescriptions">
              <div class="rx-item" *ngFor="let item of rx.items">
                <strong>💊 {{ item.medicationName }} ({{ item.dosage }})</strong>
                <span>{{ item.frequency }} — Duration: {{ item.duration }}</span>
                <span class="rx-inst" *ngIf="item.instructions"><em>Instructions: {{ item.instructions }}</em></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <ng-template #emptyHistory>
        <div class="card">
          <div class="empty-state">
            <div class="empty-state-icon">📑</div>
            <div class="empty-state-title">No Medical Records Found</div>
            <p>You have no completed consultation records on file.</p>
          </div>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .encounter-card {
      margin-bottom: 2rem;
      border-left: 4px solid var(--accent);
    }
    .encounter-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 1.25rem;
      padding-bottom: 1rem;
      border-bottom: 1px solid var(--border);
    }
    .diagnosis-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: var(--secondary);
    }
    .encounter-meta {
      font-size: 0.8125rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
      display: flex;
      gap: 0.5rem;
    }
    .dot-sep { opacity: 0.5; }
    .soap-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1.25rem;
      margin-bottom: 1.25rem;
    }
    .soap-section {
      background: #f8fafc;
      padding: 1rem;
      border-radius: var(--radius-sm);
      border: 1px solid var(--border);
    }
    .soap-title {
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      color: var(--text-muted);
      margin-bottom: 0.375rem;
    }
    .soap-text {
      font-size: 0.875rem;
      color: var(--text-main);
      line-height: 1.4;
    }
    .follow-up {
      font-weight: 700;
      color: var(--primary);
    }
    .prescriptions-box {
      margin-top: 1rem;
      padding-top: 1rem;
      border-top: 1px dashed var(--border);
    }
    .rx-header {
      font-size: 0.8125rem;
      font-weight: 700;
      color: var(--secondary);
      margin-bottom: 0.5rem;
    }
    .rx-card {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      border-radius: var(--radius-sm);
      padding: 0.75rem 1rem;
    }
    .rx-item {
      display: flex;
      flex-direction: column;
      font-size: 0.8125rem;
      margin-bottom: 0.375rem;
    }
    .rx-item:last-child { margin-bottom: 0; }
    .rx-inst { color: var(--text-muted); font-size: 0.75rem; }
  `]
})
export class PatientHistoryComponent implements OnInit {
  private authService = inject(AuthService);
  private patientService = inject(PatientService);

  consultations: Consultation[] = [];

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.patientService.getPatientConsultations(profileId).subscribe({
        next: (data) => (this.consultations = data)
      });
    }
  }
}
