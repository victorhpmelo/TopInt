import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MatButtonModule } from '@angular/material/button';
import { MatListModule } from '@angular/material/list';

@Component({
  selector: 'app-backup-panel',
  imports: [MatButtonModule, MatListModule],
  templateUrl: './backup-panel.component.html',
  styleUrl: './backup-panel.component.scss'
})
export class BackupPanelComponent {

  readonly basePath = environment.apiUrl;
  backups: string[] = [];
  message = '';
  busy = false;

  constructor(private readonly http: HttpClient) {
    this.refreshList();
  }

  runBackup(): void {
    this.message = '';
    this.busy = true;
    this.http.post<{ fileName: string; path: string }>(`${this.basePath}database/backup`, {}).subscribe({
      next: res => {
        this.busy = false;
        this.message = `Backup criado: ${res.fileName}`;
        this.refreshList();
      },
      error: () => {
        this.busy = false;
        this.message = 'Falha ao executar backup. Verifique se pg_dump está instalado e acessível no PATH.';
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.message = '';
    this.busy = true;
    const form = new FormData();
    form.append('file', file);
    this.http.post<void>(`${this.basePath}database/restore`, form).subscribe({
      next: () => {
        this.busy = false;
        this.message = 'Restauração enviada ao servidor. Pode ser necessário reiniciar a API.';
        input.value = '';
      },
      error: () => {
        this.busy = false;
        this.message = 'Falha na restauração. Verifique o arquivo e se o psql está no PATH.';
        input.value = '';
      }
    });
  }

  private refreshList(): void {
    this.http.get<string[]>(`${this.basePath}database/backups`).subscribe({
      next: rows => this.backups = rows,
      error: () => this.backups = []
    });
  }
}
