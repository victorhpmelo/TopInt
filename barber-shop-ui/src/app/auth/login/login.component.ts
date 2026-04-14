import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-login',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatCardModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent implements OnInit, OnDestroy {

  private static readonly LOCK_DURATION_SECONDS = 10 * 60;
  private static readonly LOCK_STORAGE_KEY = 'auth.lockedUntil';
  private countdownId: ReturnType<typeof setInterval> | null = null;

  readonly form = new FormGroup({
    username: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100)]
    }),
    password: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(6), Validators.maxLength(255)]
    })
  });

  errorMessage = '';
  submitting = false;
  lockRemainingSeconds = 0;

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    const rawLockedUntil = localStorage.getItem(LoginComponent.LOCK_STORAGE_KEY);
    if (!rawLockedUntil) {
      return;
    }

    const lockedUntil = Number(rawLockedUntil);
    if (!Number.isFinite(lockedUntil)) {
      localStorage.removeItem(LoginComponent.LOCK_STORAGE_KEY);
      return;
    }

    const remaining = Math.ceil((lockedUntil - Date.now()) / 1000);
    if (remaining > 0) {
      this.startLockCountdown(remaining);
      return;
    }

    localStorage.removeItem(LoginComponent.LOCK_STORAGE_KEY);
  }

  ngOnDestroy(): void {
    this.clearLockTimer();
  }

  get usernameCtrl(): FormControl<string> {
    return this.form.controls.username;
  }

  get passwordCtrl(): FormControl<string> {
    return this.form.controls.password;
  }

  submit(): void {
    this.errorMessage = '';
    if (this.lockRemainingSeconds > 0) {
      this.errorMessage = `Conta temporariamente bloqueada. Tente novamente em ${this.formatRemainingTime(this.lockRemainingSeconds)}.`;
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    const { username, password } = this.form.getRawValue();
    this.auth.login(username, password).subscribe({
      next: () => {
        this.clearLockTimer();
        this.lockRemainingSeconds = 0;
        localStorage.removeItem(LoginComponent.LOCK_STORAGE_KEY);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') || '/schedules/month';
        void this.router.navigateByUrl(returnUrl);
      },
      error: err => {
        this.submitting = false;
        if (err?.status === 423) {
          this.startLockCountdown(LoginComponent.LOCK_DURATION_SECONDS);
          return;
        }
        const msg = err?.error?.message ?? err?.message ?? 'Falha ao autenticar.';
        this.errorMessage = typeof msg === 'string' ? msg : 'Falha ao autenticar.';
      }
    });
  }

  private startLockCountdown(seconds: number): void {
    this.clearLockTimer();
    this.lockRemainingSeconds = seconds;
    localStorage.setItem(
      LoginComponent.LOCK_STORAGE_KEY,
      String(Date.now() + seconds * 1000)
    );
    this.errorMessage = `Conta temporariamente bloqueada. Tente novamente em ${this.formatRemainingTime(this.lockRemainingSeconds)}.`;

    this.countdownId = setInterval(() => {
      this.lockRemainingSeconds -= 1;
      if (this.lockRemainingSeconds <= 0) {
        this.clearLockTimer();
        this.lockRemainingSeconds = 0;
        localStorage.removeItem(LoginComponent.LOCK_STORAGE_KEY);
        this.errorMessage = '';
        return;
      }
      this.errorMessage = `Conta temporariamente bloqueada. Tente novamente em ${this.formatRemainingTime(this.lockRemainingSeconds)}.`;
    }, 1000);
  }

  private clearLockTimer(): void {
    if (this.countdownId) {
      clearInterval(this.countdownId);
      this.countdownId = null;
    }
  }

  private formatRemainingTime(totalSeconds: number): string {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
  }
}
