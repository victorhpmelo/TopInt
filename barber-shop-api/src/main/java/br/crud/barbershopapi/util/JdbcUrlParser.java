package br.crud.barbershopapi.util;

import java.util.regex.Pattern;

public final class JdbcUrlParser {

    private static final Pattern PG = Pattern.compile("jdbc:postgresql://([^/:]+)(?::(\\d+))?/([^?]+)");

    private JdbcUrlParser() {
    }

    public record PgTarget(String host, int port, String database) {
    }

    public static PgTarget parsePostgresql(final String jdbcUrl) {
        final var m = PG.matcher(jdbcUrl);
        if (!m.find()) {
            throw new IllegalArgumentException("URL JDBC do PostgreSQL inválida: " + jdbcUrl);
        }
        final String host = m.group(1);
        final int port = m.group(2) != null ? Integer.parseInt(m.group(2)) : 5432;
        final String database = m.group(3);
        return new PgTarget(host, port, database);
    }
}
