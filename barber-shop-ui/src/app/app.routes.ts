import { Routes } from '@angular/router';
import { EditClientComponent } from './clients/edit-client/edit-client.component';
import { SchedulesMonthComponent } from './schedules/schedules-month/schedules-month.component';
import { ListClientsComponent } from './clients/list-clients/list-clients.component';
import { NewClientComponent } from './clients/new-client/new-client.component';
import { LoginComponent } from './auth/login/login.component';
import { authGuard, adminGuard } from './auth/auth.guard';
import { UsersManageComponent } from './users/users-manage/users-manage.component';
import { UserFormComponent } from './users/user-form/user-form.component';
import { BackupPanelComponent } from './admin/backup-panel/backup-panel.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, data: { title: 'Login' } },
  { path: 'clients/edit-client/:id', component: EditClientComponent, canActivate: [authGuard], data: { title: 'Atualizar Cliente' } },
  { path: 'clients/new-client', component: NewClientComponent, canActivate: [authGuard], data: { title: 'Cadastrar de Cliente' } },
  { path: 'clients/list', component: ListClientsComponent, canActivate: [authGuard], data: { title: 'Clientes Cadastrados' } },
  { path: 'schedules/month', component: SchedulesMonthComponent, canActivate: [authGuard], data: { title: 'Agendamentos' } },
  { path: 'users', component: UsersManageComponent, canActivate: [adminGuard], data: { title: 'Usuários da aplicação' } },
  { path: 'users/new', component: UserFormComponent, canActivate: [adminGuard], data: { title: 'Novo usuário' } },
  { path: 'users/edit/:id', component: UserFormComponent, canActivate: [adminGuard], data: { title: 'Editar usuário' } },
  { path: 'admin/backup', component: BackupPanelComponent, canActivate: [adminGuard], data: { title: 'Backup e restauração' } },
  { path: '', pathMatch: 'full', redirectTo: 'schedules/month' },
  { path: '**', redirectTo: 'schedules/month' }
];
