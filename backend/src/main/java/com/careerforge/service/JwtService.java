package com.careerforge.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private static final long EXPIRATION_TIME = 60L * 60 * 1000;
    private final SecretKey secretKey;
    private final com.careerforge.repository.UserRepository users;
    private final com.careerforge.recovery.PasswordRecoveryRepository recoveries;
    public JwtService(@Value("${careerforge.jwt.secret:}") String secret, Environment environment, com.careerforge.recovery.PasswordRecoveryRepository recoveries, com.careerforge.repository.UserRepository users) {
        this.recoveries = recoveries; this.users = users;
        if (secret.isBlank()) {
            if (environment.acceptsProfiles(Profiles.of("prod"))) throw new IllegalStateException("JWT_SECRET is required in production");
            try {
                var path = java.nio.file.Path.of(".local", "jwt-signing.key");
                java.nio.file.Files.createDirectories(path.getParent());
                if (!java.nio.file.Files.exists(path)) {
                    var generated = java.util.Base64.getEncoder().encodeToString(Jwts.SIG.HS256.key().build().getEncoded());
                    try { java.nio.file.Files.writeString(path, generated, java.nio.file.StandardOpenOption.CREATE_NEW); }
                    catch (java.nio.file.FileAlreadyExistsException ignored) { /* Another startup created it. */ }
                }
                secretKey = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(java.nio.file.Files.readString(path).trim()));
            } catch (java.io.IOException exception) { throw new IllegalStateException("Unable to load the local signing key", exception); }
        } else {
            if (secret.getBytes(StandardCharsets.UTF_8).length < 32) throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
            secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
    }
    public String generateToken(Long userId, String email) {
        return Jwts.builder().subject(String.valueOf(userId)).claim("email",email).claim("sv", sessionVersion(userId)).issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME)).signWith(secretKey).compact();
    }
    public Long extractUserId(String token) {
        Claims claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
        return Long.valueOf(claims.getSubject());
    }
    private long sessionVersion(Long id) { return recoveries.findById(id).map(state -> state.sessionVersion).orElse(0L); }
    public boolean isTokenValid(String token) {
        try { var claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
            Number version = claims.get("sv", Number.class);
            return users.existsById(Long.valueOf(claims.getSubject())) && (version == null ? 0L : version.longValue()) == sessionVersion(Long.valueOf(claims.getSubject())); }
        catch (Exception exception) { return false; }
    }
}

