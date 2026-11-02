package com.careerforge.recovery;

import com.careerforge.repository.UserRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class PasswordRecoveryService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PasswordRecoveryService.class);
    private final BrevoRecoveryMailer brevo;
    private final UserRepository users;
    private final PasswordRecoveryRepository recoveries;
    private final PasswordEncoder passwords;
    private final ObjectProvider<JavaMailSender> mail;
    private final ThreadPoolTaskExecutor executor;
    private final TransactionTemplate transaction;
    private final String from;
    private final String frontend;
    private final boolean enabled;

    public PasswordRecoveryService(UserRepository users, PasswordRecoveryRepository recoveries,
            PasswordEncoder passwords, ObjectProvider<JavaMailSender> mail, BrevoRecoveryMailer brevo,
            ThreadPoolTaskExecutor recoveryExecutor, PlatformTransactionManager transactions,
            @Value("${careerforge.recovery.enabled:false}") boolean enabled,
            @Value("${careerforge.recovery.from:}") String from,
            @Value("${careerforge.recovery.frontend-url:}") String frontend) {
        this.users = users; this.recoveries = recoveries; this.passwords = passwords;
        this.brevo = brevo;
        this.mail = mail; this.executor = recoveryExecutor; this.transaction = new TransactionTemplate(transactions);
        this.enabled = enabled; this.from = from; this.frontend = frontend.replaceAll("/+$", "");
        if (enabled) {
            var uri = java.net.URI.create(this.frontend);
            boolean local = Set.of("localhost", "127.0.0.1").contains(Objects.toString(uri.getHost(), ""));
            if (uri.getHost() == null || uri.getQuery() != null || uri.getFragment() != null
                    || uri.getUserInfo() != null || (!"https".equals(uri.getScheme()) && !(local && "http".equals(uri.getScheme()))))
                throw new IllegalArgumentException("PASSWORD_RESET_FRONTEND_URL must be a trusted HTTPS website URL");
        }
    }

    public void request(String email) {
        if (!enabled || from.isBlank() || frontend.isBlank() || (!brevo.configured() && mail.getIfAvailable() == null))
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Password recovery is not available yet. Please try again later.");
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        // Queue both known and unknown addresses; HTTP timing does not reveal account existence.
        try { executor.execute(() -> {
            try { transaction.executeWithoutResult(status -> issue(normalized)); }
            catch (Exception exception) {
                // Never log email bodies, reset tokens, passwords or SMTP credentials.
                log.error("Password recovery delivery failed; check email provider configuration and service availability. Failure category: {}", exception.getClass().getSimpleName());
            }
        }); } catch (org.springframework.core.task.TaskRejectedException exception) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Recovery is busy. Please try again shortly.");
        }
    }

    private void issue(String email) {
        var found = users.findByEmail(email);
        if (found.isEmpty()) { log.info("Password recovery: request skipped; no matching account in this database."); return; }
        var user = users.lockById(found.get().getId()).orElseThrow();
        var state = recoveries.lockByUserId(user.getId()).orElseGet(PasswordRecovery::new);
        var now = Instant.now();
        // Persist the cooldown so duplicate requests across servers cannot flood the mailbox.
        if (state.requestedAt != null && state.requestedAt.plusSeconds(120).isAfter(now)) { log.info("Password recovery: request skipped; cooldown active."); return; }
        byte[] random = new byte[32]; new SecureRandom().nextBytes(random);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        state.userId = user.getId(); state.tokenHash = hash(token);
        state.expiresAt = now.plusSeconds(1800); state.requestedAt = now;
        recoveries.saveAndFlush(state);
        var message = new SimpleMailMessage();
        message.setFrom(from); message.setTo(user.getEmail());
        message.setSubject("Reset your CareerForge password");
        message.setText("Use this link to choose a new password:\n\n" + frontend + "/reset-password#token=" + token
            + "\n\nThis link expires in 30 minutes and can be used once. If you did not request it, ignore this email. Your password has not changed.");
        log.info("Password recovery: submitting email via {}.", brevo.configured() ? "Brevo HTTPS" : "SMTP");
        if (brevo.configured()) brevo.send(message);
        else mail.getObject().send(message);
        log.info("Password recovery: provider accepted email; inbox delivery is not yet confirmed.");
    }

    public void reset(String token, String password) {
        if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ResponseStatusException(BAD_REQUEST, "Use at least 8 characters and at most 72 UTF-8 bytes.");
        if (token == null || !token.matches("[A-Za-z0-9_-]{43}")) throw invalid();
        String digest = hash(token);
        transaction.executeWithoutResult(status -> {
            var found = recoveries.findByTokenHash(digest).orElseThrow(PasswordRecoveryService::invalid);
            var user = users.lockById(found.userId).orElseThrow(PasswordRecoveryService::invalid);
            // Re-read after the user lock: concurrent redemption must never succeed twice.
            var state = recoveries.findById(user.getId()).orElseThrow(PasswordRecoveryService::invalid);
            // Refresh because the initial token lookup may already be in this persistence context.
            entityManager.refresh(state, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            if (!digest.equals(state.tokenHash) || state.expiresAt == null || !state.expiresAt.isAfter(Instant.now())) throw invalid();
            user.setPassword(passwords.encode(password));
            state.tokenHash = null; state.expiresAt = null; state.sessionVersion++;
            users.save(user); recoveries.save(state);
        });
    }

    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
    private static ResponseStatusException invalid() {
        return new ResponseStatusException(BAD_REQUEST, "This reset link is invalid or expired. Please request a new link.");
    }
    static String hash(String token) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException exception) { throw new IllegalStateException(exception); }
    }
}
