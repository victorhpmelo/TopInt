package br.crud.barbershopapi.config;

import br.crud.barbershopapi.models.AppUserModel;
import br.crud.barbershopapi.models.AppUserRole;
import br.crud.barbershopapi.repositories.IAppUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@AllArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private static final String DEFAULT_ADMIN_USER = "admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@Pass1234";

    private final IAppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(final ApplicationArguments args) {
        if (userRepository.existsByUsernameIgnoreCase(DEFAULT_ADMIN_USER)) {
            return;
        }
        final Instant now = Instant.now();
        final AppUserModel admin = AppUserModel.builder()
                .username(DEFAULT_ADMIN_USER)
                .passwordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .fullName("Administrador")
                .role(AppUserRole.ADMIN)
                .active(true)
                .consecutiveFailures(0)
                .failuresToday(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userRepository.save(admin);
    }
}
