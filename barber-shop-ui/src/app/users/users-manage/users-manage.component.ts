import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { UsersService } from '../../services/api-client/users/users.service';
import { AppUserResponse } from '../../services/api-client/users/users.models';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
@Component({
  selector: 'app-users-manage',
  imports: [MatTableModule, MatButtonModule, MatIconModule, RouterLink],
  templateUrl: './users-manage.component.html',
  styleUrl: './users-manage.component.scss'
})
export class UsersManageComponent implements OnInit {

  displayedColumns = ['username', 'fullName', 'role', 'active', 'actions'];
  users: AppUserResponse[] = [];
  loadError = '';

  constructor(
    private readonly usersService: UsersService,
    private readonly router: Router
  ) { }

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loadError = '';
    this.usersService.list().subscribe({
      next: rows => this.users = rows,
      error: () => this.loadError = 'Não foi possível carregar os usuários.'
    });
  }

  edit(id: number): void {
    void this.router.navigate(['/users/edit', id]);
  }

  remove(u: AppUserResponse): void {
    if (!confirm(`Excluir o usuário ${u.username}?`)) {
      return;
    }
    this.usersService.delete(u.id).subscribe({
      next: () => this.reload(),
      error: () => alert('Falha ao excluir usuário.')
    });
  }
}
