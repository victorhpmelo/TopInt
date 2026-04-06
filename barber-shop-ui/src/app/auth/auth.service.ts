import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AUTH_ROLE_KEY, AUTH_TOKEN_KEY, AUTH_USER_KEY } from './auth.constants';

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  expiresInMs: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly basePath = environment.apiUrl;

  constructor(private readonly http: HttpClient) { }

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.basePath}auth/login`, { username, password })
      .pipe(tap(res => this.persistSession(res)));
  }

  logout(): void {
    sessionStorage.removeItem(AUTH_TOKEN_KEY);
    sessionStorage.removeItem(AUTH_ROLE_KEY);
    sessionStorage.removeItem(AUTH_USER_KEY);
  }

  getToken(): string | null {
    return sessionStorage.getItem(AUTH_TOKEN_KEY);
  }

  getUsername(): string | null {
    return sessionStorage.getItem(AUTH_USER_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    return sessionStorage.getItem(AUTH_ROLE_KEY) === 'ADMIN';
  }

  private persistSession(res: LoginResponse): void {
    sessionStorage.setItem(AUTH_TOKEN_KEY, res.token);
    sessionStorage.setItem(AUTH_ROLE_KEY, res.role);
    sessionStorage.setItem(AUTH_USER_KEY, res.username);
  }
}
