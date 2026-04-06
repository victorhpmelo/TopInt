package br.crud.barbershopapi.services.impl;

import br.crud.barbershopapi.audit.AuditLogService;
import br.crud.barbershopapi.models.ClientModel;
import br.crud.barbershopapi.repositories.IClientRepository;
import br.crud.barbershopapi.services.IClientService;
import br.crud.barbershopapi.services.query.IClientQueryService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class ClientService implements IClientService {

    private final IClientRepository repository;
    private final IClientQueryService queryService;
    private final AuditLogService auditLogService;

    @Override
    public ClientModel save(final ClientModel model) {
        queryService.verifyEmail(model.getEmail());
        queryService.verifyPhone(model.getPhone());

        final ClientModel saved = repository.save(model);
        auditLogService.log(
                "BUSINESS_CLIENT",
                currentActor(),
                "Inclusão de novo cliente: " + saved.getName() + "."
        );
        return saved;
    }

    @Override
    public ClientModel update(final ClientModel model) {
        queryService.verifyEmail(model.getId(), model.getEmail());
        queryService.verifyPhone(model.getId(), model.getPhone());

        var stored = queryService.findById(model.getId());
        stored.setName(model.getName());
        stored.setPhone(model.getPhone());
        stored.setEmail(model.getEmail());
        final ClientModel saved = repository.save(stored);
        auditLogService.log(
                "BUSINESS_CLIENT",
                currentActor(),
                "Alteração de dados do cliente: " + saved.getName() + "."
        );
        return saved;
    }

    @Override
    public void delete(final long id) {
        final var existing = queryService.findById(id);
        final String name = existing.getName();
        repository.deleteById(id);
        auditLogService.log(
                "BUSINESS_CLIENT",
                currentActor(),
                "Exclusão do cliente: " + name + "."
        );
    }

    private static String currentActor() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "system";
        }
        return auth.getName();
    }
}
