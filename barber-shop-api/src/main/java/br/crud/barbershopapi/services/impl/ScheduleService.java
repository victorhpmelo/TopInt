package br.crud.barbershopapi.services.impl;

import br.crud.barbershopapi.audit.AuditLogService;
import br.crud.barbershopapi.models.ScheduleModel;
import br.crud.barbershopapi.repositories.IScheduleRepository;
import br.crud.barbershopapi.services.IScheduleService;
import br.crud.barbershopapi.services.query.IScheduleQueryService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class ScheduleService implements IScheduleService {

    private final IScheduleRepository repository;
    private final IScheduleQueryService queryService;
    private final AuditLogService auditLogService;

    @Override
    public ScheduleModel save(final ScheduleModel model) {
        queryService.verifyIfScheduleExists(model.getStartAt(), model.getEndAt());

        final ScheduleModel saved = repository.save(model);
        final String clientName = saved.getClient() != null && saved.getClient().getName() != null
                ? saved.getClient().getName()
                : "cliente " + saved.getClient().getId();
        auditLogService.log(
                "BUSINESS_SCHEDULE",
                currentActor(),
                "Novo agendamento registrado para " + clientName + " entre " + saved.getStartAt() + " e " + saved.getEndAt() + "."
        );
        return saved;
    }

    @Override
    public void delete(final long id) {
        final var existing = queryService.findbyId(id);
        final String clientName = existing.getClient() != null && existing.getClient().getName() != null
                ? existing.getClient().getName()
                : "cliente " + existing.getClient().getId();
        repository.deleteById(id);
        auditLogService.log(
                "BUSINESS_SCHEDULE",
                currentActor(),
                "Cancelamento de agendamento do cliente " + clientName + " entre " + existing.getStartAt() + " e " + existing.getEndAt() + "."
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