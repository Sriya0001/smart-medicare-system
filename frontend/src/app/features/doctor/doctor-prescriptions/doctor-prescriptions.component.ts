import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { DoctorService } from '../../../core/services/doctor.service';
import { ConsultationService } from '../../../core/services/consultation.service';
import { Prescription } from '../../../models/consultation.models';

@Component({
  selector: 'app-doctor-prescriptions',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Issued Prescriptions Log</h1>
          <p class="page-subtitle">Master audit trail of all electronic medications and regimens prescribed by you</p>
        </div>
      </div>

      <div class="card">
        <div class="table-responsive" *ngIf="prescriptions && prescriptions.length > 0; else noRx">
          <table class="table">
            <thead>
              <tr>
                <th>Rx #</th>
                <th>Issue Date</th>
                <th>Patient Name</th>
                <th>Prescribed Items</th>
                <th>Notes</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let rx of prescriptions">
                <td><strong>#{{ rx.id }}</strong></td>
                <td>{{ rx.issueDate }}</td>
                <td><strong>{{ rx.patientName }}</strong></td>
                <td>
                  <div *ngFor="let item of rx.items" class="med-line">
                    • <strong>{{ item.medicationName }}</strong> ({{ item.dosage }}) — {{ item.frequency }} ({{ item.duration }})
                  </div>
                </td>
                <td>{{ rx.notes || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <ng-template #noRx>
          <div class="empty-state">
            <div class="empty-state-icon">💊</div>
            <div class="empty-state-title">No Prescriptions Issued Yet</div>
            <p>Prescriptions created during patient consultations will be logged here.</p>
          </div>
        </ng-template>
      </div>
    </div>
  `,
  styles: [`
    .med-line {
      font-size: 0.8125rem;
      margin-bottom: 0.25rem;
    }
  `]
})
export class DoctorPrescriptionsComponent implements OnInit {
  private authService = inject(AuthService);
  private consultationService = inject(ConsultationService);

  prescriptions: Prescription[] = [];

  ngOnInit(): void {
    const profileId = this.authService.profileId;
    if (profileId) {
      this.consultationService.getAllPrescriptions().subscribe({
        next: (all) => {
          this.prescriptions = all.filter(p => p.doctorId === profileId);
        }
      });
    }
  }
}
