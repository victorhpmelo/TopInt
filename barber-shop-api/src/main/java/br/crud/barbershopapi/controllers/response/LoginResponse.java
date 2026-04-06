package br.crud.barbershopapi.controllers.response;

public record LoginResponse(
        String token,
        String username,
        String role,
        long expiresInMs
) {
}
