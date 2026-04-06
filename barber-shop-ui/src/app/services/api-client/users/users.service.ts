import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { AppUserResponse, CreateAppUserRequest, UpdateAppUserRequest } from './users.models';

@Injectable({
  providedIn: 'root'
})
export class UsersService {

  private readonly basePath = environment.apiUrl;

  constructor(private readonly http: HttpClient) { }

  list(): Observable<AppUserResponse[]> {
    return this.http.get<AppUserResponse[]>(`${this.basePath}users`);
  }

  findById(id: number): Observable<AppUserResponse> {
    return this.http.get<AppUserResponse>(`${this.basePath}users/${id}`);
  }

  create(request: CreateAppUserRequest): Observable<AppUserResponse> {
    return this.http.post<AppUserResponse>(`${this.basePath}users`, request);
  }

  update(id: number, request: UpdateAppUserRequest): Observable<AppUserResponse> {
    return this.http.put<AppUserResponse>(`${this.basePath}users/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.basePath}users/${id}`);
  }
}
