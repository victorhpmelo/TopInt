import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, ReactiveFormsModule, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UsersService } from '../../services/api-client/users/users.service';
import { AppUserRole } from '../../services/api-client/users/users.models';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-user-form',
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    RouterLink
  ],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss'
})
export class UserFormComponent implements OnInit {

  readonly roles: AppUserRole[] = ['ADMIN', 'STAFF'];
  private readonly passwordPolicy = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z\d]).{10,}$/;

  readonly form = new FormGroup({
    username: new FormControl<string>({ value: '', disabled: true }, {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(100), Validators.pattern(/^\S+$/)]
    }),
    fullName: new FormControl<string>('', {
      nonNullable: true,
      validators: [Validators.required, Validators.minLength(3), Validators.maxLength(255)]
    }),
    role: new FormControl<AppUserRole>('STAFF', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl<string>('', {
      nonNullable: true,
      validators: [this.passwordPolicyValidator(false)]
    }),
    newPassword: new FormControl<string>('', {
      nonNullable: true,
      validators: [this.passwordPolicyValidator(true)]
    })
  });

  isCreate = true;
  userId: number | null = null;
  errorMessage = '';
  submitting = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly usersService: UsersService
  ) { }

  get usernameCtrl(): FormControl<string> {
    return this.form.controls.username;
  }

  get fullNameCtrl(): FormControl<string> {
    return this.form.controls.fullName;
  }

  get passwordCtrl(): FormControl<string> {
    return this.form.controls.password;
  }

  get newPasswordCtrl(): FormControl<string> {
    return this.form.controls.newPassword;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (!idParam) {
      this.isCreate = true;
      this.form.controls.username.enable();
      this.form.controls.password.setValidators([Validators.required, this.passwordPolicyValidator(false)]);
      this.form.controls.password.updateValueAndValidity();
      return;
    }
    this.isCreate = false;
    this.userId = Number(idParam);
    this.usersService.findById(this.userId).subscribe({
      next: u => {
        this.form.patchValue({
          username: u.username,
          fullName: u.fullName,
          role: u.role
        });
      },
      error: () => {
        this.errorMessage = 'Usuário não encontrado.';
      }
    });
  }

  submit(): void {
    this.errorMessage = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting = true;
    if (this.isCreate) {
      const raw = this.form.getRawValue();
      this.usersService.create({
        username: raw.username.trim(),
        fullName: raw.fullName.trim(),
        password: raw.password,
        role: raw.role
      }).subscribe({
        next: () => void this.router.navigate(['/users']),
        error: err => this.handleError(err)
      });
      return;
    }
    if (this.userId == null) {
      return;
    }
    const raw = this.form.getRawValue();
    const body: { fullName: string; role: AppUserRole; newPassword?: string } = {
      fullName: raw.fullName.trim(),
      role: raw.role
    };
    if (raw.newPassword?.trim()) {
      body.newPassword = raw.newPassword;
    }
    this.usersService.update(this.userId, body).subscribe({
      next: () => void this.router.navigate(['/users']),
      error: err => this.handleError(err)
    });
  }

  private handleError(err: unknown): void {
    this.submitting = false;
    const anyErr = err as { error?: { message?: string } };
    const msg = anyErr?.error?.message ?? 'Falha ao salvar.';
    this.errorMessage = typeof msg === 'string' ? msg : 'Falha ao salvar.';
  }

  private passwordPolicyValidator(optional: boolean): ValidatorFn {
    return (control: AbstractControl<string>) => {
      const value = control.value ?? '';
      if (!value.trim()) {
        return optional ? null : { required: true };
      }
      return this.passwordPolicy.test(value)
        ? null
        : { passwordPolicy: true };
    };
  }
}
