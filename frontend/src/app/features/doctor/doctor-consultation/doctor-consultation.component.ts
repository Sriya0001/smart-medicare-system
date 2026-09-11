import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AppointmentService } from '../../../core/services/appointment.service';
import { ConsultationService } from '../../../core/services/consultation.service';
import { ToastService } from '../../../core/services/toast.service';
import { Appointment } from '../../../models/appointment.models';
import { AiSummary } from '../../../models/consultation.models';

@Component({
  selector: 'app-doctor-consultation',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="page-wrapper" *ngIf="appointment">
      <div class="page-header">
        <div>
          <h1 class="page-title">Clinical Consultation Encounter</h1>
          <p class="page-subtitle">Patient: <strong>{{ appointment.patientName }}</strong> — Appointment #{{ appointment.id }}</p>
        </div>
        <button type="button" class="btn btn-secondary" (click)="toggleAiSummary()">
          <span>🤖</span>
          <span>{{ showAiSummary ? 'Hide AI History Summary' : 'Summarize Patient History (AI)' }}</span>
        </button>
      </div>

      <!-- Patient Demographics Banner -->
      <div class="card patient-banner">
        <div class="patient-banner-grid">
          <div><span class="label">Patient:</span> <strong>{{ appointment.patientName }}</strong></div>
          <div><span class="label">Contact:</span> {{ appointment.patientPhone || 'N/A' }}</div>
          <div><span class="label">Email:</span> {{ appointment.patientEmail }}</div>
          <div><span class="label">Scheduled For:</span> {{ appointment.appointmentDate }} ({{ appointment.startTime }} – {{ appointment.endTime }})</div>
          <div class="full-row"><span class="label">Reason for Visit:</span> {{ appointment.reason }}</div>
        </div>
      </div>

      <!-- AI Clinical History Summary Drawer/Card -->
      <div class="card ai-summary-card" *ngIf="showAiSummary">
        <div class="card-header">
          <span class="card-title">🤖 Clinical History Summary</span>
          <span class="badge badge-primary">Documentation Support</span>
        </div>

        <div *ngIf="loadingAi" class="empty-state">
          <p>Analyzing prior patient consultation notes...</p>
        </div>

        <div *ngIf="!loadingAi && aiSummary" class="ai-content">
          <div class="ai-grid">
            <div class="ai-box">
              <div class="ai-label">Past Chief Complaints:</div>
              <pre class="ai-text">{{ aiSummary.chiefComplaintsSummary }}</pre>
            </div>
            <div class="ai-box">
              <div class="ai-label">Diagnosis History:</div>
              <pre class="ai-text">{{ aiSummary.diagnosisHistory }}</pre>
            </div>
            <div class="ai-box">
              <div class="ai-label">Previous Treatments Administered:</div>
              <pre class="ai-text">{{ aiSummary.treatmentsSummary }}</pre>
            </div>
            <div class="ai-box">
              <div class="ai-label">Pending Follow-Up Directives:</div>
              <pre class="ai-text">{{ aiSummary.followUpDirectives }}</pre>
            </div>
          </div>
          <div class="ai-disclaimer">
            * AI documentation assistance tool — for clinical reference only. Does not replace physician diagnosis.
          </div>
        </div>
      </div>

      <!-- SOAP Clinical Notes & Prescription Form -->
      <form [formGroup]="consultationForm" (ngSubmit)="onSubmit()">
        <div class="card">
          <div class="card-header">
            <span class="card-title">SOAP Clinical Documentation</span>
          </div>

          <div class="form-group">
            <label class="form-label">Subjective / Chief Complaint & Symptoms *</label>
            <textarea class="form-control" formControlName="symptoms" placeholder="Record patient symptoms, history of present illness, onset, duration..."></textarea>
          </div>

          <div class="form-group">
            <label class="form-label">Assessment / Primary Diagnosis *</label>
            <input type="text" class="form-control" formControlName="diagnosis" placeholder="e.g. Essential Hypertension, Acute Bronchitis, Type 2 Diabetes Mellitus">
          </div>

          <div class="form-group">
            <label class="form-label">Plan / Treatment Plan & Directives</label>
            <textarea class="form-control" formControlName="treatmentPlan" placeholder="Lifestyle modifications, dietary directives, physical therapy, diagnostic imaging..."></textarea>
          </div>

          <div class="grid-2">
            <div class="form-group">
              <label class="form-label">Objective / Clinical Examination Notes</label>
              <textarea class="form-control" formControlName="notes" placeholder="Vital signs, auscultation findings, physical exam observations..."></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">Next Scheduled Follow-Up Date</label>
              <input type="date" class="form-control" formControlName="followUpDate">
            </div>
          </div>
        </div>

        <!-- Electronic Prescription Section -->
        <div class="card">
          <div class="card-header">
            <span class="card-title">Electronic Prescription (e-Rx)</span>
            <button type="button" class="btn btn-outline btn-sm" (click)="addPrescriptionItem()">
              + Add Medication
            </button>
          </div>

          <div formArrayName="prescriptionItems">
            <div *ngIf="prescriptionItems.length === 0" class="empty-state">
              <p>No medications added. Click "+ Add Medication" to attach an electronic prescription.</p>
            </div>

            <div class="rx-item-row" *ngFor="let item of prescriptionItems.controls; let i = index" [formGroupName]="i">
              <div class="rx-grid">
                <div class="form-group">
                  <label class="form-label">Medication Name *</label>
                  <input type="text" class="form-control" formControlName="medicationName" placeholder="e.g. Amoxicillin, Lisinopril">
                </div>
                <div class="form-group">
                  <label class="form-label">Dosage *</label>
                  <input type="text" class="form-control" formControlName="dosage" placeholder="e.g. 500 mg, 10 ml">
                </div>
                <div class="form-group">
                  <label class="form-label">Frequency *</label>
                  <input type="text" class="form-control" formControlName="frequency" placeholder="e.g. Twice daily with meals">
                </div>
                <div class="form-group">
                  <label class="form-label">Duration *</label>
                  <input type="text" class="form-control" formControlName="duration" placeholder="e.g. 7 days, 30 days">
                </div>
              </div>
              <div class="rx-sub-row">
                <input type="text" class="form-control" formControlName="instructions" placeholder="Specific patient instructions (e.g. Take with water after breakfast)">
                <button type="button" class="btn btn-danger btn-sm" (click)="removePrescriptionItem(i)">Remove</button>
              </div>
            </div>
          </div>
        </div>

        <div class="submit-bar">
          <button type="submit" class="btn btn-primary btn-lg" [disabled]="consultationForm.invalid || submitting">
            <span *ngIf="!submitting">Finalize Consultation & Complete Appointment</span>
            <span *ngIf="submitting">Finalizing...</span>
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .patient-banner {
      background: #f8fafc;
      border-left: 4px solid var(--primary);
    }
    .patient-banner-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 0.75rem;
      font-size: 0.875rem;
    }
    .label {
      color: var(--text-muted);
      font-size: 0.75rem;
      text-transform: uppercase;
      font-weight: 600;
      display: block;
    }
    .full-row {
      grid-column: 1 / -1;
      padding-top: 0.5rem;
      border-top: 1px solid var(--border);
    }
    .ai-summary-card {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
    }
    .ai-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
      gap: 1rem;
      margin-bottom: 0.75rem;
    }
    .ai-box {
      background: #ffffff;
      padding: 0.875rem;
      border-radius: var(--radius-sm);
      border: 1px solid #dcfce7;
    }
    .ai-label {
      font-size: 0.75rem;
      font-weight: 700;
      color: var(--success);
      margin-bottom: 0.25rem;
      text-transform: uppercase;
    }
    .ai-text {
      font-family: inherit;
      font-size: 0.8125rem;
      color: var(--text-main);
      white-space: pre-wrap;
      line-height: 1.4;
    }
    .ai-disclaimer {
      font-size: 0.6875rem;
      color: var(--text-muted);
      font-style: italic;
    }
    .rx-item-row {
      background: #f8fafc;
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      padding: 1rem;
      margin-bottom: 1rem;
    }
    .rx-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
      gap: 0.75rem;
    }
    .rx-sub-row {
      display: flex;
      gap: 0.75rem;
      align-items: center;
      margin-top: 0.5rem;
    }
    .submit-bar {
      margin-top: 2rem;
      display: flex;
      justify-content: flex-end;
    }
    .btn-lg {
      padding: 0.875rem 2rem;
      font-size: 1rem;
    }
  `]
})
export class DoctorConsultationComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private appointmentService = inject(AppointmentService);
  private consultationService = inject(ConsultationService);
  private toastService = inject(ToastService);

  appointment: Appointment | null = null;
  aiSummary: AiSummary | null = null;
  showAiSummary = false;
  loadingAi = false;
  submitting = false;

  consultationForm = this.fb.group({
    symptoms: ['', Validators.required],
    diagnosis: ['', Validators.required],
    treatmentPlan: [''],
    notes: [''],
    followUpDate: [''],
    prescriptionItems: this.fb.array([])
  });

  get prescriptionItems(): FormArray {
    return this.consultationForm.get('prescriptionItems') as FormArray;
  }

  ngOnInit(): void {
    const apptId = Number(this.route.snapshot.paramMap.get('appointmentId'));
    if (apptId) {
      this.appointmentService.getAppointmentById(apptId).subscribe({
        next: (appt) => {
          this.appointment = appt;
          // Pre-populate subjective symptoms with reason if present
          this.consultationForm.patchValue({
            symptoms: appt.reason
          });
        }
      });
    }
  }

  addPrescriptionItem(): void {
    const itemGroup = this.fb.group({
      medicationName: ['', Validators.required],
      dosage: ['', Validators.required],
      frequency: ['', Validators.required],
      duration: ['', Validators.required],
      instructions: ['']
    });
    this.prescriptionItems.push(itemGroup);
  }

  removePrescriptionItem(index: number): void {
    this.prescriptionItems.removeAt(index);
  }

  toggleAiSummary(): void {
    this.showAiSummary = !this.showAiSummary;
    if (this.showAiSummary && !this.aiSummary && this.appointment) {
      this.loadingAi = true;
      this.consultationService.getPatientHistorySummary(this.appointment.patientId).subscribe({
        next: (data) => {
          this.aiSummary = data;
          this.loadingAi = false;
        },
        error: () => {
          this.loadingAi = false;
        }
      });
    }
  }

  onSubmit(): void {
    if (this.consultationForm.invalid || !this.appointment) return;

    this.submitting = true;
    const formVal = this.consultationForm.getRawValue();

    this.consultationService.createConsultation({
      appointmentId: this.appointment.id,
      symptoms: formVal.symptoms!,
      diagnosis: formVal.diagnosis!,
      treatmentPlan: formVal.treatmentPlan || undefined,
      notes: formVal.notes || undefined,
      followUpDate: formVal.followUpDate || undefined,
      prescriptionItems: formVal.prescriptionItems && formVal.prescriptionItems.length > 0 ? formVal.prescriptionItems as any : undefined
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.toastService.success('Consultation record finalized and appointment marked completed.');
        this.router.navigate(['/doctor/dashboard']);
      },
      error: () => {
        this.submitting = false;
      }
    });
  }
}
