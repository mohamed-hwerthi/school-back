package com.schoolSys.schooolSys.auth;

import java.util.UUID;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtTokenProvider {

    /** HS256 requires a key of at least 256 bits (32 bytes / characters). */
    private static final int MIN_SECRET_LENGTH = 32;

    /** The throw-away secret shipped in application-dev.yml — never allowed in prod. */
    private static final String DEV_DEFAULT_SECRET =
            "aVeryLongSecretKeyThatIsAtLeast256BitsForHS256Algorithm2026!";

    private final SecretKey key;
    private final long accessTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:}") String secret,
            @Value("${app.jwt.access-expiration-ms:900000}") long accessTokenExpirationMs,
            Environment environment) {
        boolean prodProfile = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        validateSecret(secret, prodProfile);
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    /**
     * Fails application startup with a clear message when the JWT secret is
     * missing, too weak, or — in production — left as the development default.
     */
    private static void validateSecret(String secret, boolean prodProfile) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.secret est vide : definissez la variable d'environnement "
                            + "JWT_SECRET (au moins " + MIN_SECRET_LENGTH + " caracteres).");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "app.jwt.secret est trop court (" + secret.length() + " caracteres) : "
                            + "JWT_SECRET doit faire au moins " + MIN_SECRET_LENGTH + " caracteres.");
        }
        if (prodProfile && DEV_DEFAULT_SECRET.equals(secret)) {
            throw new IllegalStateException(
                    "Le secret JWT de developpement est utilise en profil prod : "
                            + "definissez un JWT_SECRET unique et confidentiel pour la production.");
        }
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMs);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("tenantId", user.getTenantId())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }

    public String getTenantIdFromToken(String token) {
        return parseToken(token).get("tenantId", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    /** Expiry instant carried by the token, used to bound blacklist entries. */
    public Instant getExpiration(String token) {
        return parseToken(token).getExpiration().toInstant();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
