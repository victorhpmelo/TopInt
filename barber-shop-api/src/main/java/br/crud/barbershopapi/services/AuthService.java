package br.crud.barbershopapi.services;

import br.crud.barbershopapi.audit.AuditLogService;
import br.crud.barbershopapi.models.AppUserModel;
import br.crud.barbershopapi.repositories.IAppUserRepository;
import br.crud.barbershopapi.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class AuthService {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private static final int MAX_FAILURES = 5;
    private static final int LOCK_MINUTES = 10;

    private final IAppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    @Transactional
    public LoginResult login(final String username, final String rawPassword) {
        final var userOpt = userRepository.findByUsernameIgnoreCase(username.trim());
        if (userOpt.isEmpty()) {
            auditLogService.log("AUTH_FAILURE", username, "Erro de autenticação: usuário não encontrado.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }
        final AppUserModel user = userOpt.get();
        if (!user.isActive()) {
            auditLogService.log("AUTH_FAILURE", user.getUsername(), "Erro de autenticação: usuário inativo.");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuário inativo.");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            auditLogService.log("AUTH_FAILURE", user.getUsername(), "Tentativa de login com conta bloqueada.");
            throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Conta bloqueada temporariamente. Tente novamente após o horário indicado."
            );
        }

        final boolean matches = passwordEncoder.matches(rawPassword, user.getPasswordHash());
        if (!matches) {
            registerFailedAttempt(user);
            auditLogService.log("AUTH_FAILURE", user.getUsername(), "Erro de autenticação: senha incorreta.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas.");
        }

        user.setConsecutiveFailures(0);
        user.setFailuresToday(0);
        user.setLastFailureDate(null);
        user.setLockedUntil(null);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        final String token = jwtService.generateToken(user.getUsername(), user.getRole());
        return new LoginResult(token, user.getUsername(), user.getRole().name());
    }

    private void registerFailedAttempt(final AppUserModel user) {
        final LocalDate today = LocalDate.now(ZONE);
        if (user.getLastFailureDate() == null || !user.getLastFailureDate().equals(today)) {
            user.setFailuresToday(1);
            user.setConsecutiveFailures(1);
        } else {
            user.setFailuresToday(user.getFailuresToday() + 1);
            user.setConsecutiveFailures(user.getConsecutiveFailures() + 1);
        }
        user.setLastFailureDate(today);
        user.setUpdatedAt(Instant.now());

        if (user.getFailuresToday() == MAX_FAILURES) {
            auditLogService.log(
                    "AUTH_FAILURE_SAME_DAY",
                    user.getUsername(),
                    "Registro de 5 falhas de autenticação no mesmo dia para o usuário."
            );
        }

        if (user.getConsecutiveFailures() == MAX_FAILURES) {
            user.setLockedUntil(Instant.now().plus(LOCK_MINUTES, ChronoUnit.MINUTES));
            user.setConsecutiveFailures(0);
            auditLogService.log(
                    "ACCOUNT_LOCKED",
                    user.getUsername(),
                    "Conta bloqueada por 10 minutos após 5 falhas consecutivas de autenticação."
            );
        }
        userRepository.save(user);
    }

    public record LoginResult(String token, String username, String role) {
    }
}
