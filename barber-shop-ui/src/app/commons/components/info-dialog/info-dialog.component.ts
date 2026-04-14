import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-info-dialog',
  imports: [MatDialogModule, MatButtonModule],
  templateUrl: './info-dialog.component.html',
  styleUrl: './info-dialog.component.scss'
})
export class InfoDialogComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA)
    readonly data: { title: string; content: string; tone?: 'success' | 'error' | 'info' }
  ) { }
}
