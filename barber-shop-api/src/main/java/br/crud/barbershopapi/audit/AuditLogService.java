package br.crud.barbershopapi.audit;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@Log4j2
public class AuditLogService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private static final ZoneId ZONE = ZoneId.systemDefault();

    private final Path logPath;

    public AuditLogService(@Value("${app.audit.log-file:logs/audit.log}") final String logFile) {
        this.logPath = Path.of(logFile).toAbsolutePath().normalize();
    }

    public void log(final String eventType, final String username, final String description) {
        final String line = FMT.format(OffsetDateTime.now(ZONE))
                + " | " + eventType
                + " | " + (username == null ? "-" : username)
                + " | " + description.replace("\n", " ")
                + System.lineSeparator();
        synchronized (AuditLogService.class) {
            try {
                final Path parent = logPath.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Files.writeString(
                        logPath,
                        line,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                );
            } catch (final IOException e) {
                log.error("Falha ao gravar log de auditoria em {}", logPath, e);
            }
        }
    }
}
