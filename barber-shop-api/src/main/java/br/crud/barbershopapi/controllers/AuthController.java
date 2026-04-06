package br.crud.barbershopapi.controllers;

import br.crud.barbershopapi.controllers.request.LoginRequest;
import br.crud.barbershopapi.controllers.response.LoginResponse;
import br.crud.barbershopapi.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final long jwtExpirationMs;

    public AuthController(
            final AuthService authService,
            @Value("${app.jwt.expiration-ms:3600000}") final long jwtExpirationMs
    ) {
        this.authService = authService;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    @PostMapping("/login")
    LoginResponse login(@RequestBody @Valid final LoginRequest request) {
        final var result = authService.login(request.username(), request.password());
        return new LoginResponse(result.token(), result.username(), result.role(), jwtExpirationMs);
    }
}
