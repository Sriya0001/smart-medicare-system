import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DoctorService } from '../../../core/services/doctor.service';
import { Doctor } from '../../../models/doctor.models';

@Component({
  selector: 'app-doctor-directory',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  template: `
    <div class="page-wrapper">
      <div class="page-header">
        <div>
          <h1 class="page-title">Medical Specialists Directory</h1>
          <p class="page-subtitle">Find board-certified doctors across multiple medical specialties</p>
        </div>
      </div>

      <!-- Filter & Search Toolbar -->
      <div class="card filter-card">
        <div class="filter-row">
          <div class="search-box">
            <input type="text" class="form-control" [(ngModel)]="searchQuery" (input)="onSearch()" placeholder="Search doctors by name or specialty...">
          </div>
          <div class="select-box">
            <select class="form-select" [(ngModel)]="selectedSpecialty" (change)="onSpecialtyChange()">
              <option value="">All Specialties</option>
              <option *ngFor="let spec of specialties" [value]="spec">{{ spec }}</option>
            </select>
          </div>
        </div>
      </div>

      <!-- Doctors Grid -->
      <div class="grid-3" *ngIf="doctors && doctors.length > 0; else noDocs">
        <div class="card doctor-card" *ngFor="let doc of doctors">
          <div class="doc-header">
            <div class="doc-avatar">{{ doc.firstName.charAt(0) }}{{ doc.lastName.charAt(0) }}</div>
            <div class="doc-info">
              <h3 class="doc-name">{{ doc.fullName }}</h3>
              <span class="badge badge-primary">{{ doc.specialty }}</span>
            </div>
          </div>

          <p class="doc-bio">{{ doc.biography || 'Experienced clinician providing quality patient care.' }}</p>

          <div class="doc-meta">
            <div class="meta-item">
              <span class="meta-label">Experience</span>
              <span class="meta-value">{{ doc.experienceYears || 5 }}+ Years</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">Consultation</span>
              <span class="meta-value">\${{ doc.consultationFee || 100 }}</span>
            </div>
          </div>

          <div class="doc-actions">
            <a [routerLink]="['/patient/book-appointment']" [queryParams]="{ doctorId: doc.id }" class="btn btn-primary btn-block">
              📅 Book Appointment
            </a>
          </div>
        </div>
      </div>

      <ng-template #noDocs>
        <div class="card">
          <div class="empty-state">
            <div class="empty-state-icon">🔍</div>
            <div class="empty-state-title">No Doctors Found</div>
            <p>Try adjusting your search criteria or selecting a different specialty.</p>
          </div>
        </div>
      </ng-template>
    </div>
  `,
  styles: [`
    .filter-card {
      padding: 1.25rem;
      margin-bottom: 2rem;
    }
    .filter-row {
      display: flex;
      gap: 1rem;
      flex-wrap: wrap;
    }
    .search-box {
      flex: 2;
      min-width: 250px;
    }
    .select-box {
      flex: 1;
      min-width: 200px;
    }
    .doctor-card {
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .doc-header {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1rem;
    }
    .doc-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary) 0%, var(--accent) 100%);
      color: #ffffff;
      font-weight: 700;
      font-size: 1.125rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .doc-name {
      font-size: 1.0625rem;
      font-weight: 700;
      color: var(--secondary);
      margin-bottom: 0.25rem;
    }
    .doc-bio {
      font-size: 0.8125rem;
      color: var(--text-muted);
      line-height: 1.4;
      margin-bottom: 1rem;
      flex: 1;
    }
    .doc-meta {
      display: flex;
      justify-content: space-between;
      padding: 0.75rem 0;
      border-top: 1px solid var(--border);
      border-bottom: 1px solid var(--border);
      margin-bottom: 1rem;
    }
    .meta-item {
      display: flex;
      flex-direction: column;
    }
    .meta-label {
      font-size: 0.6875rem;
      text-transform: uppercase;
      color: var(--text-sub);
      font-weight: 600;
    }
    .meta-value {
      font-size: 0.9375rem;
      font-weight: 700;
      color: var(--secondary);
    }
    .btn-block {
      width: 100%;
    }
  `]
})
export class DoctorDirectoryComponent implements OnInit {
  private doctorService = inject(DoctorService);

  doctors: Doctor[] = [];
  specialties: string[] = [];
  searchQuery = '';
  selectedSpecialty = '';

  ngOnInit(): void {
    this.loadSpecialties();
    this.loadDoctors();
  }

  loadSpecialties(): void {
    this.doctorService.getAllSpecialties().subscribe({
      next: (data) => (this.specialties = data)
    });
  }

  loadDoctors(): void {
    if (this.selectedSpecialty) {
      this.doctorService.getAllDoctors(this.selectedSpecialty).subscribe({
        next: (data) => (this.doctors = data)
      });
    } else {
      this.doctorService.getAllDoctors().subscribe({
        next: (data) => (this.doctors = data)
      });
    }
  }

  onSpecialtyChange(): void {
    this.searchQuery = '';
    this.loadDoctors();
  }

  onSearch(): void {
    if (this.searchQuery.trim().length > 0) {
      this.doctorService.searchDoctors(this.searchQuery).subscribe({
        next: (data) => (this.doctors = data)
      });
    } else {
      this.loadDoctors();
    }
  }
}
