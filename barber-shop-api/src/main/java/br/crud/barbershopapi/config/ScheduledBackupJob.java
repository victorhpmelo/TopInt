package br.crud.barbershopapi.config;

import br.crud.barbershopapi.services.DatabaseBackupService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@AllArgsConstructor
public class ScheduledBackupJob {

    private final DatabaseBackupService databaseBackupService;

    @Scheduled(cron = "${app.backup.cron:0 0 2 * * *}")
    public void runScheduledBackup() {
        try {
            databaseBackupService.runManualBackup();
        } catch (final Exception e) {
            log.error("Falha no backup agendado da base de dados.", e);
        }
    }
}
