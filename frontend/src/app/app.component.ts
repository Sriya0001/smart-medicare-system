import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar.component';
import { ToastContainerComponent } from './shared/components/toast-container/toast-container.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    NavbarComponent,
    SidebarComponent,
    ToastContainerComponent
  ],
  template: `
    <app-toast-container></app-toast-container>
    <app-navbar></app-navbar>

    <div class="app-container">
      <app-sidebar *ngIf="authService.currentUser$ | async"></app-sidebar>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `
})
export class AppComponent {
  authService = inject(AuthService);
}
