package br.crud.barbershopapi.controllers.request;

import br.crud.barbershopapi.models.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAppUserRequest(
        @NotBlank @Size(max = 255) String fullName,
        AppUserRole role,
        String newPassword
) {
}
