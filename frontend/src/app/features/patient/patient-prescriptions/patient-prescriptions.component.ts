import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { PatientService } from '../../../core/services/patient.service';
import { ConsultationService } from '../../../core/services/consultation.service';
import { Prescription, PrescriptionExplanation } from '../../../models/consultation.models';

@Component({
  selector: 'app-patient-prescriptions',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">My Prescriptions</h1>
          <p class="page-subtitle">View your e-prescriptions and get AI-powered plain-language guides on how to take them safely.</p>
        </div>
      </div>

      <div *ngIf="prescriptions && prescriptions.length > 0; else emptyRx">
        <div class="card rx-full-card" *ngFor="let rx of prescriptions">
          <div class="rx-card-header">
            <div>
              <div class="rx-id">Prescription #{{ rx.id }}</div>
              <div class="rx-doc">Prescribed by <strong>{{ rx.doctorName }}</strong></div>
            </div>
            <div class="rx-header-right">
              <div class="rx-date-box">
                <span class="rx-issue-label">Issue Date:</span>
                <span class="rx-issue-val">{{ rx.issueDate }}</span>
              </div>
              <button
                class="btn-ai-explain"
                (click)="toggleExplanation(rx.id)"
                [disabled]="loadingExplanationId === rx.id"
              >
                <span *ngIf="loadingExplanationId === rx.id" class="spinner-small"></span>
                <span *ngIf="loadingExplanationId !== rx.id">✨</span>
                {{ expandedExplanationId === rx.id ? 'Hide Plain Guide' : 'Explain in Plain Terms' }}
              </button>
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

          <!-- AI Plain-Language Prescription Explainer Section -->
          <div class="ai-guide-container" *ngIf="expandedExplanationId === rx.id">
            <div *ngIf="loadingExplanationId === rx.id" class="ai-loading">
              <div class="spinner-pulse"></div>
              <p>Consulting Gemini AI to translate prescription details into plain English...</p>
            </div>

            <div *ngIf="explanations[rx.id] as exp" class="ai-guide-content">
              <div class="ai-guide-header">
                <div class="ai-badge">
                  <span>✨ AI Medication Companion</span>
                  <span class="badge-tag" *ngIf="exp.isAiGenerated">Gemini AI</span>
                </div>
                <div class="ai-overview">{{ exp.overview }}</div>
              </div>

              <div class="ai-meds-grid">
                <div class="ai-med-card" *ngFor="let m of exp.medications">
                  <div class="ai-med-title">💊 {{ m.medicationName }}</div>
                  
                  <div class="ai-med-field">
                    <div class="ai-field-label">🎯 Why You Are Taking It:</div>
                    <div class="ai-field-text">{{ m.purpose }}</div>
                  </div>

                  <div class="ai-med-field">
                    <div class="ai-field-label">⏰ How & When to Take:</div>
                    <div class="ai-field-text">{{ m.howToTake }}</div>
                  </div>

                  <div class="ai-med-field" *ngIf="m.commonSideEffects">
                    <div class="ai-field-label">💡 Common Side Effects & Tips:</div>
                    <div class="ai-field-text">{{ m.commonSideEffects }}</div>
                  </div>
                </div>
              </div>

              <div class="ai-extra-grid">
                <div class="ai-extra-card advice-card" *ngIf="exp.dietaryAndLifestyleAdvice">
                  <div class="ai-extra-header">🥗 Diet & Lifestyle Tips</div>
                  <div class="ai-extra-body">{{ exp.dietaryAndLifestyleAdvice }}</div>
                </div>

                <div class="ai-extra-card alert-card" *ngIf="exp.whenToCallDoctor">
                  <div class="ai-extra-header">📞 When to Contact Doctor</div>
                  <div class="ai-extra-body">{{ exp.whenToCallDoctor }}</div>
                </div>
              </div>
            </div>
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
      flex-wrap: wrap;
      gap: 1rem;
    }
    .rx-header-right {
      display: flex;
      align-items: center;
      gap: 1.25rem;
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
    .btn-ai-explain {
      display: inline-flex;
      align-items: center;
      gap: 0.4rem;
      background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
      color: #ffffff;
      border: none;
      padding: 0.45rem 0.9rem;
      border-radius: 9999px;
      font-size: 0.8125rem;
      font-weight: 600;
      cursor: pointer;
      box-shadow: 0 2px 4px rgba(79, 70, 229, 0.2);
      transition: all 0.2s ease;
    }
    .btn-ai-explain:hover {
      background: linear-gradient(135deg, #4f46e5 0%, #4338ca 100%);
      transform: translateY(-1px);
      box-shadow: 0 4px 8px rgba(79, 70, 229, 0.3);
    }
    .btn-ai-explain:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
    .spinner-small {
      width: 12px;
      height: 12px;
      border: 2px solid #ffffff;
      border-top-color: transparent;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
      display: inline-block;
    }
    .rx-notes {
      margin-top: 1rem;
      padding: 0.75rem;
      background-color: #f8fafc;
      border-radius: var(--radius-sm);
      font-size: 0.8125rem;
      color: var(--text-main);
    }

    /* AI Explainer Box Styling */
    .ai-guide-container {
      margin-top: 1.25rem;
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      border-radius: 12px;
      padding: 1.25rem;
      animation: fadeIn 0.3s ease-in-out;
    }
    .ai-loading {
      text-align: center;
      padding: 1.5rem;
      color: #166534;
      font-size: 0.875rem;
      font-weight: 500;
    }
    .spinner-pulse {
      width: 28px;
      height: 28px;
      margin: 0 auto 0.75rem auto;
      border: 3px solid #86efac;
      border-top-color: #16a34a;
      border-radius: 50%;
      animation: spin 0.9s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(-6px); }
      to { opacity: 1; transform: translateY(0); }
    }
    .ai-badge {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 0.875rem;
      font-weight: 700;
      color: #15803d;
      margin-bottom: 0.35rem;
    }
    .badge-tag {
      background-color: #dcfce7;
      border: 1px solid #86efac;
      padding: 0.15rem 0.5rem;
      border-radius: 6px;
      font-size: 0.7rem;
      color: #166534;
      text-transform: uppercase;
      letter-spacing: 0.04em;
    }
    .ai-overview {
      font-size: 0.9rem;
      color: #14532d;
      line-height: 1.45;
      margin-bottom: 1.25rem;
    }
    .ai-meds-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .ai-med-card {
      background: #ffffff;
      border: 1px solid #dcfce7;
      border-radius: 8px;
      padding: 1rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
    }
    .ai-med-title {
      font-weight: 700;
      font-size: 0.95rem;
      color: #0f172a;
      margin-bottom: 0.75rem;
      padding-bottom: 0.35rem;
      border-bottom: 1px solid #f1f5f9;
    }
    .ai-med-field {
      margin-bottom: 0.65rem;
    }
    .ai-field-label {
      font-size: 0.725rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      color: #64748b;
      margin-bottom: 0.15rem;
    }
    .ai-field-text {
      font-size: 0.825rem;
      color: #334155;
      line-height: 1.4;
    }
    .ai-extra-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 1rem;
    }
    .ai-extra-card {
      padding: 0.85rem 1rem;
      border-radius: 8px;
      font-size: 0.825rem;
      line-height: 1.4;
    }
    .advice-card {
      background-color: #fefce8;
      border: 1px solid #fef08a;
      color: #713f12;
    }
    .advice-card .ai-extra-header {
      font-weight: 700;
      color: #854d0e;
      margin-bottom: 0.3rem;
    }
    .alert-card {
      background-color: #fff1f2;
      border: 1px solid #fecdd3;
      color: #881337;
    }
    .alert-card .ai-extra-header {
      font-weight: 700;
      color: #9f1239;
      margin-bottom: 0.3rem;
    }
  `]
})
export class PatientPrescriptionsComponent implements OnInit {
  private authService = inject(AuthService);
  private patientService = inject(PatientService);
  private consultationService = inject(ConsultationService);

  prescriptions: Prescription[] = [];
  explanations: { [prescriptionId: number]: PrescriptionExplanation } = {};
  expandedExplanationId: number | null = null;
  loadingExplanationId: number | null = null;

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.patientService.getPatientPrescriptions(profileId).subscribe({
        next: (data) => (this.prescriptions = data)
      });
    }
  }

  toggleExplanation(prescriptionId: number): void {
    if (this.expandedExplanationId === prescriptionId) {
      this.expandedExplanationId = null;
      return;
    }

    this.expandedExplanationId = prescriptionId;

    if (!this.explanations[prescriptionId]) {
      this.loadingExplanationId = prescriptionId;
      this.consultationService.getPrescriptionExplanation(prescriptionId).subscribe({
        next: (data) => {
          this.explanations[prescriptionId] = data;
          this.loadingExplanationId = null;
        },
        error: (err) => {
          console.error('Failed to load prescription explanation', err);
          this.loadingExplanationId = null;
        }
      });
    }
  }
}
