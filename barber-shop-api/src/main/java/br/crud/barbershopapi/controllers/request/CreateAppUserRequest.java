package br.crud.barbershopapi.controllers.request;

import br.crud.barbershopapi.models.AppUserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAppUserRequest(
        @NotBlank @Size(max = 100) String username,
        @NotBlank @Size(max = 255) String fullName,
        @NotBlank String password,
        @NotNull AppUserRole role
) {
}
