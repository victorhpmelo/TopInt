import { Component } from '@angular/core';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../auth/auth.service';

@Component({
  selector: 'app-menu-bar',
  imports: [MatMenuModule, MatButtonModule, RouterLink],
  templateUrl: './menu-bar.component.html',
  styleUrl: './menu-bar.component.scss'
})
export class MenuBarComponent {

  constructor(
    private readonly router: Router,
    readonly auth: AuthService
  ) { }

  navigateTo(path: string): void {
    void this.router.navigate([path]);
  }

  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
