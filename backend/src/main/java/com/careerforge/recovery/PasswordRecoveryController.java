package com.careerforge.recovery;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class PasswordRecoveryController {
    private final PasswordRecoveryService service;
    private final Map<String, Window> windows = new HashMap<>();
    private record Window(long start, int count) {}
    public record Forgot(@NotBlank @Email @Size(max=254) String email) {}
    public record Reset(@NotBlank @Size(max=100) String token, @NotBlank @Size(max=72) String password) {}
    public PasswordRecoveryController(PasswordRecoveryService service) { this.service = service; }
    @PostMapping("/forgot-password")
    public Map<String, String> forgot(@Valid @RequestBody Forgot input, HttpServletRequest request) {
        limit(request.getRemoteAddr());
        service.request(input.email());
        return Map.of("message", "Request received. If this email belongs to an account, we will attempt to send a reset link. Check your inbox and spam folder. You can try again after 2 minutes.");
    }
    @PostMapping("/reset-password")
    public Map<String, String> reset(@Valid @RequestBody Reset input, HttpServletRequest request) {
        limit(request.getRemoteAddr());
        service.reset(input.token(), input.password());
        return Map.of("message", "Your password has been updated. Sign in with your new password. Your saved progress is unchanged.");
    }
    private synchronized void limit(String key) {
        long now = System.currentTimeMillis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().start() >= 60_000);
        var window = windows.get(key);
        if ((window != null && window.count() >= 20) || (window == null && windows.size() >= 10_000))
            throw new ResponseStatusException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Please wait a minute and try again.");
        windows.put(key, new Window(window == null ? now : window.start(), window == null ? 1 : window.count() + 1));
    }
}
