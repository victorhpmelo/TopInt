package br.crud.barbershopapi.controllers.response;

import br.crud.barbershopapi.models.AppUserRole;

public record AppUserResponse(
        long id,
        String username,
        String fullName,
        AppUserRole role,
        boolean active
) {
}
