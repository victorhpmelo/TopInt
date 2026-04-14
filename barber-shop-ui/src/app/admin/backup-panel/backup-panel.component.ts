import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { MatListModule } from '@angular/material/list';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { YesNoDialogComponent } from '../../commons/components/yes-no-dialog/yes-no-dialog.component';
import { InfoDialogComponent } from '../../commons/components/info-dialog/info-dialog.component';

@Component({
  selector: 'app-backup-panel',
  imports: [MatListModule, MatButtonModule],
  templateUrl: './backup-panel.component.html',
  styleUrl: './backup-panel.component.scss'
})
export class BackupPanelComponent {

  readonly basePath = environment.apiUrl;
  backups: string[] = [];
  message = '';
  busy = false;
  selectedFile: File | null = null;

  constructor(
    private readonly http: HttpClient,
    private readonly dialog: MatDialog
  ) {
    this.refreshList();
  }

  runBackup(): void {
    if (this.busy) {
      return;
    }

    const ref = this.dialog.open(YesNoDialogComponent, {
      width: '420px',
      data: {
        title: 'Confirmar backup manual',
        content: 'Deseja iniciar o backup agora? Esta ação pode levar alguns segundos.'
      }
    });

    ref.afterClosed().subscribe(confirm => {
      if (!confirm) {
        return;
      }

      this.message = '';
      this.busy = true;
      this.http.post<{ fileName: string; path: string }>(`${this.basePath}database/backup`, {}).subscribe({
        next: res => {
          this.busy = false;
          this.message = `Backup criado: ${res.fileName}`;
          this.openInfo('Backup concluido', this.message, 'success');
          this.refreshList();
        },
        error: () => {
          this.busy = false;
          this.message = 'Falha ao executar backup. Verifique logs da API e pg_dump.';
          this.openInfo('Falha no backup', this.message, 'error');
        }
      });
    });
  }

  openFilePicker(input: HTMLInputElement): void {
    if (this.busy) {
      return;
    }
    input.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }
    this.selectedFile = file;
    this.message = `Arquivo selecionado: ${file.name}`;
  }

  runRestore(input: HTMLInputElement): void {
    if (this.busy) {
      return;
    }
    if (!this.selectedFile) {
      this.openInfo('Arquivo obrigatorio', 'Selecione um arquivo .sql antes de restaurar.', 'info');
      return;
    }

    const file = this.selectedFile;
    const ref = this.dialog.open(YesNoDialogComponent, {
      width: '420px',
      data: {
        title: 'Confirmar restauracao',
        content: `Deseja restaurar o banco usando o arquivo ${file.name}?`
      }
    });

    ref.afterClosed().subscribe(confirm => {
      if (!confirm) {
        return;
      }

      const form = new FormData();
      form.append('file', file);

      this.message = '';
      this.busy = true;
      this.http.post<void>(`${this.basePath}database/restore`, form).subscribe({
        next: () => {
          this.busy = false;
          this.selectedFile = null;
          input.value = '';
          this.message = 'Restauracao enviada ao servidor. Aguarde alguns segundos.';
          this.openInfo('Restauracao enviada', this.message, 'success');
          this.refreshList();
        },
        error: () => {
          this.busy = false;
          this.message = 'Falha na restauracao. Verifique o arquivo SQL e os logs da API.';
          this.openInfo('Falha na restauracao', this.message, 'error');
          input.value = '';
          this.selectedFile = null;
        }
      });
    });
  }

  clearSelectedFile(input: HTMLInputElement): void {
    this.selectedFile = null;
    input.value = '';
    this.message = '';
  }

  private refreshList(): void {
    this.http.get<string[]>(`${this.basePath}database/backups`).subscribe({
      next: rows => this.backups = rows,
      error: () => this.backups = []
    });
  }

  private openInfo(title: string, content: string, tone: 'success' | 'error' | 'info'): void {
    this.dialog.open(InfoDialogComponent, {
      width: '430px',
      data: { title, content, tone }
    });
  }
}
