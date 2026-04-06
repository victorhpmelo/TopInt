package br.crud.barbershopapi.services;

import br.crud.barbershopapi.util.JdbcUrlParser;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class DatabaseBackupService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final String jdbcUrl;
    private final String dbUser;
    private final String dbPassword;
    private final Path backupDirectory;
    private final String pgDumpCommand;
    private final String psqlCommand;

    public DatabaseBackupService(
            @Value("${spring.datasource.url}") final String jdbcUrl,
            @Value("${spring.datasource.username}") final String dbUser,
            @Value("${spring.datasource.password}") final String dbPassword,
            @Value("${app.backup.directory:./backups}") final String backupDirectory,
            @Value("${app.backup.pg-dump-command:pg_dump}") final String pgDumpCommand,
            @Value("${app.backup.psql-command:psql}") final String psqlCommand
    ) {
        this.jdbcUrl = jdbcUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.backupDirectory = Path.of(backupDirectory).toAbsolutePath().normalize();
        this.pgDumpCommand = pgDumpCommand;
        this.psqlCommand = psqlCommand;
    }

    public Path runManualBackup() throws IOException, InterruptedException {
        Files.createDirectories(backupDirectory);
        final JdbcUrlParser.PgTarget t = JdbcUrlParser.parsePostgresql(jdbcUrl);
        final String name = "backup-" + FMT.format(LocalDateTime.now()) + ".sql";
        final Path out = backupDirectory.resolve(name);
        final ProcessBuilder pb = new ProcessBuilder(
                pgDumpCommand,
                "-h", t.host(),
                "-p", String.valueOf(t.port()),
                "-U", dbUser,
                "-d", t.database(),
                "-Fp",
                "-f", out.toString()
        );
        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);
        final Process p = pb.start();
        final String output = new String(p.getInputStream().readAllBytes());
        final int code = p.waitFor();
        if (code != 0) {
            Files.deleteIfExists(out);
            throw new IllegalStateException("pg_dump falhou (código " + code + "): " + output);
        }
        return out;
    }

    public List<String> listBackups() throws IOException {
        if (!Files.isDirectory(backupDirectory)) {
            return List.of();
        }
        try (Stream<Path> s = Files.list(backupDirectory)) {
            return s.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".sql"))
                    .sorted(Comparator.reverseOrder())
                    .toList();
        }
    }

    public void restore(final MultipartFile file) throws IOException, InterruptedException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de backup é obrigatório.");
        }
        final Path temp = Files.createTempFile("restore-", ".sql");
        try {
            file.transferTo(temp);
            final JdbcUrlParser.PgTarget t = JdbcUrlParser.parsePostgresql(jdbcUrl);
            final ProcessBuilder pb = new ProcessBuilder(
                    psqlCommand,
                    "-h", t.host(),
                    "-p", String.valueOf(t.port()),
                    "-U", dbUser,
                    "-d", t.database(),
                    "-v", "ON_ERROR_STOP=1",
                    "-f", temp.toAbsolutePath().toString()
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);
            final Process p = pb.start();
            final String output = new String(p.getInputStream().readAllBytes());
            final int code = p.waitFor();
            if (code != 0) {
                throw new IllegalStateException("psql restore falhou (código " + code + "): " + output);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
