import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import { AuthResponse, LoginRequest, RegisterRequest, Role } from '../../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly API_URL = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'medicare_auth_token';
  private readonly USER_KEY = 'medicare_auth_user';

  private currentUserSubject = new BehaviorSubject<AuthResponse | null>(this.getStoredUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  public get currentUserValue(): AuthResponse | null {
    return this.currentUserSubject.value;
  }

  public get token(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  public get isAuthenticated(): boolean {
    return !!this.token && !!this.currentUserValue;
  }

  public get role(): Role | null {
    return this.currentUserValue ? this.currentUserValue.role : null;
  }

  public get profileId(): number | null {
    return this.currentUserValue ? this.currentUserValue.profileId : null;
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, request).pipe(
      tap(res => this.setSession(res))
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, request).pipe(
      tap(res => this.setSession(res))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  public updateStoredUser(updated: Partial<AuthResponse>): void {
    if (this.currentUserValue) {
      const merged = { ...this.currentUserValue, ...updated };
      localStorage.setItem(this.USER_KEY, JSON.stringify(merged));
      this.currentUserSubject.next(merged);
    }
  }

  private setSession(authResponse: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResponse.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(authResponse));
    this.currentUserSubject.next(authResponse);
  }

  private getStoredUser(): AuthResponse | null {
    const raw = localStorage.getItem(this.USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }
}
