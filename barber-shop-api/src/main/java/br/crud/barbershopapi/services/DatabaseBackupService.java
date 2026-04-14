package br.crud.barbershopapi.services;

import br.crud.barbershopapi.util.JdbcUrlParser;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
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
@Log4j2
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

    // REQUISITO: Backup Agendado (Executa todos os dias às 02:00 da manhã)
    // Formato CRON: segundo minuto hora dia mês dia-da-semana
    // "0 0 2 * * ?" = 02:00 AM todos os dias
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledDailyBackup() {
        try {
            log.info("Iniciando backup agendado do banco de dados");
            runManualBackup();
            log.info("Backup agendado completado com sucesso");
        } catch (final Exception e) {
            log.error("Erro ao executar backup agendado: {}", e.getMessage(), e);
        }
    }

    // REQUISITO: Backup Manual (Pode ser chamado por um Controller/Endpoint a qualquer momento)
    public Path runManualBackup() throws IOException, InterruptedException {
        // Cria o diretório de backups se não existir
        Files.createDirectories(backupDirectory);
        
        // Faz parsing da URL JDBC para extrair detalhes do banco de dados (host, porta, nome)
        final JdbcUrlParser.PgTarget t = JdbcUrlParser.parsePostgresql(jdbcUrl);
        
        // Gera um nome de arquivo único usando timestamp: backup-YYYYMMDD-HHmmss.sql
        final String name = "backup-" + FMT.format(LocalDateTime.now()) + ".sql";
        
        // Define o caminho completo onde o arquivo será salvo
        final Path out = backupDirectory.resolve(name);
        
        // Constrói o comando PostgreSQL pg_dump com parâmetros de autenticação
        final ProcessBuilder pb = new ProcessBuilder(
                pgDumpCommand,
                "-h", t.host(),
                "-p", String.valueOf(t.port()),
                "-U", dbUser,
                "-d", t.database(),
            "-Fp",  // Formato: plain text SQL
            "-c",  // Inclui DROP dos objetos antes de recriá-los no restore
            "--if-exists",  // Evita erro ao remover objetos que ainda nao existam
                "-f", out.toString()  // Arquivo de saída
        );
        
        // Define variável de ambiente PGPASSWORD para autenticação sem prompt
        pb.environment().put("PGPASSWORD", dbPassword);
        
        // Redireciona stream de erro para saída padrão para captura de mensagens
        pb.redirectErrorStream(true);
        
        // Inicia o processo e espera sua conclusão
        final Process p = pb.start();
        
        // Captura toda a saída do processo (logs/erros do pg_dump)
        final String output = new String(p.getInputStream().readAllBytes());
        
        // Aguarda a conclusão do processo e obtém o código de saída
        final int code = p.waitFor();
        
        // Se código != 0, indica erro na execução
        if (code != 0) {
            // Remove o arquivo se houve falha
            Files.deleteIfExists(out);
            throw new IllegalStateException("pg_dump falhou (código " + code + "): " + output);
        }
        
        log.info("Backup do PostgreSQL realizado com sucesso em: {}", out);
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

    // REQUISITO: Restauração do Banco de Dados a partir de um arquivo de backup
    public void restore(final MultipartFile file) throws IOException, InterruptedException {
        // Valida se o arquivo foi fornecido e não está vazio
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de backup é obrigatório.");
        }
        
        // Cria um arquivo temporário para armazenar o conteúdo do backup enviado
        final Path temp = Files.createTempFile("restore-", ".sql");
        try {
            // Transfere o arquivo enviado para o arquivo temporário
            file.transferTo(temp);
            
            // Faz parsing da URL JDBC para extrair detalhes de conexão
            final JdbcUrlParser.PgTarget t = JdbcUrlParser.parsePostgresql(jdbcUrl);
            
            // Constrói o comando psql para restaurar o banco de dados
            final ProcessBuilder pb = new ProcessBuilder(
                    psqlCommand,
                    "-h", t.host(),
                    "-p", String.valueOf(t.port()),
                    "-U", dbUser,
                    "-d", t.database(),
                    "-v", "ON_ERROR_STOP=1",  // Para a execução em caso de erro
                    "-f", temp.toAbsolutePath().toString()  // Lê o arquivo SQL
            );
            
            // Define variável de ambiente PGPASSWORD para autenticação
            pb.environment().put("PGPASSWORD", dbPassword);
            
            // Redireciona stream de erro para saída padrão
            pb.redirectErrorStream(true);
            
            // Inicia o processo de restauração
            final Process p = pb.start();
            
            // Captura toda a saída do processo
            final String output = new String(p.getInputStream().readAllBytes());
            
            // Aguarda a conclusão e obtém o código de saída
            final int code = p.waitFor();
            
            // Verifica se houve sucesso
            if (code != 0) {
                throw new IllegalStateException("psql restore falhou (código " + code + "): " + output);
            }
            
            log.info("Restauração do PostgreSQL realizada com sucesso");
        } finally {
            // Sempre remove o arquivo temporário após a restauração
            Files.deleteIfExists(temp);
        }
    }
}
