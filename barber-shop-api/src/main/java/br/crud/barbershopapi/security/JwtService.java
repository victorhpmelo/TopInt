package br.crud.barbershopapi.security;

import br.crud.barbershopapi.models.AppUserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") final String secret,
            @Value("${app.jwt.expiration-ms:3600000}") final long expirationMs
    ) {
        final byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret deve ter pelo menos 32 bytes (256 bits) para HS256.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(final String username, final AppUserRole role) {
        final Instant now = Instant.now();
        final Instant exp = now.plusMillis(expirationMs);
        return Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public String extractUsername(final String token) {
        return parseClaims(token).getSubject();
    }

    public AppUserRole extractRole(final String token) {
        final String r = parseClaims(token).get("role", String.class);
        return AppUserRole.valueOf(r);
    }

    public void validate(final String token) {
        parseClaims(token);
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
