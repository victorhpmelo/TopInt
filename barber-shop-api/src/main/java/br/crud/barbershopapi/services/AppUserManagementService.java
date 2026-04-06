package br.crud.barbershopapi.services;

import br.crud.barbershopapi.audit.AuditLogService;
import br.crud.barbershopapi.controllers.request.CreateAppUserRequest;
import br.crud.barbershopapi.controllers.request.UpdateAppUserRequest;
import br.crud.barbershopapi.controllers.response.AppUserResponse;
import br.crud.barbershopapi.models.AppUserModel;
import br.crud.barbershopapi.models.PasswordHistoryModel;
import br.crud.barbershopapi.repositories.IAppUserRepository;
import br.crud.barbershopapi.repositories.IPasswordHistoryRepository;
import br.crud.barbershopapi.security.PasswordPolicyValidator;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AppUserManagementService {

    private final IAppUserRepository userRepository;
    private final IPasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyValidator passwordPolicyValidator;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<AppUserResponse> list() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AppUserResponse findById(final long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    @Transactional
    public AppUserResponse create(final CreateAppUserRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.username().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nome de usuário já está em uso.");
        }
        passwordPolicyValidator.validateOrThrow(request.password());

        final Instant now = Instant.now();
        final AppUserModel user = AppUserModel.builder()
                .username(request.username().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(request.role())
                .active(true)
                .consecutiveFailures(0)
                .failuresToday(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        final AppUserModel saved = userRepository.save(user);
        auditLogService.log(
                "USER_CREATE",
                saved.getUsername(),
                "Cadastro de novo usuário da aplicação."
        );
        return toResponse(saved);
    }

    @Transactional
    public AppUserResponse update(final long id, final UpdateAppUserRequest request) {
        final AppUserModel user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        user.setFullName(request.fullName().trim());
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (StringUtils.hasText(request.newPassword())) {
            applyPasswordChange(user, request.newPassword());
            auditLogService.log(
                    "USER_PASSWORD_CHANGE",
                    user.getUsername(),
                    "Alteração de senha do usuário."
            );
        } else {
            auditLogService.log(
                    "USER_UPDATE",
                    user.getUsername(),
                    "Alteração de dados do usuário."
            );
        }
        user.setUpdatedAt(Instant.now());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(final long id) {
        final AppUserModel user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
        final String name = user.getUsername();
        userRepository.delete(user);
        auditLogService.log(
                "USER_DELETE",
                name,
                "Exclusão de usuário da aplicação."
        );
    }

    private void applyPasswordChange(final AppUserModel user, final String rawNewPassword) {
        passwordPolicyValidator.validateOrThrow(rawNewPassword);
        if (passwordEncoder.matches(rawNewPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("A nova senha não pode ser igual à senha atual.");
        }
        final List<PasswordHistoryModel> history = passwordHistoryRepository.findTop3ByUser_IdOrderByCreatedAtDesc(user.getId());
        for (final PasswordHistoryModel h : history) {
            if (passwordEncoder.matches(rawNewPassword, h.getPasswordHash())) {
                throw new IllegalArgumentException("A nova senha não pode ser igual a uma das 3 últimas senhas utilizadas.");
            }
        }

        final Instant now = Instant.now();
        final PasswordHistoryModel row = PasswordHistoryModel.builder()
                .user(user)
                .passwordHash(user.getPasswordHash())
                .createdAt(now)
                .build();
        passwordHistoryRepository.save(row);
        trimHistory(user.getId());

        user.setPasswordHash(passwordEncoder.encode(rawNewPassword));
    }

    private void trimHistory(final long userId) {
        final List<PasswordHistoryModel> rows = passwordHistoryRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        if (rows.size() > 3) {
            passwordHistoryRepository.deleteAll(rows.subList(3, rows.size()));
        }
    }

    private AppUserResponse toResponse(final AppUserModel u) {
        return new AppUserResponse(
                u.getId(),
                u.getUsername(),
                u.getFullName(),
                u.getRole(),
                u.isActive()
        );
    }
}
