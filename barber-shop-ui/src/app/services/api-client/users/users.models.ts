export type AppUserRole = 'ADMIN' | 'STAFF';

export interface AppUserResponse {
  id: number;
  username: string;
  fullName: string;
  role: AppUserRole;
  active: boolean;
}

export interface CreateAppUserRequest {
  username: string;
  fullName: string;
  password: string;
  role: AppUserRole;
}

export interface UpdateAppUserRequest {
  fullName: string;
  role?: AppUserRole;
  newPassword?: string;
}
