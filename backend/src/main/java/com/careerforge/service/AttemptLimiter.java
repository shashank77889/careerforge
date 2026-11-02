package com.careerforge.service;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.*;
@Component
public class AttemptLimiter {
 private record Window(long start, int count) {}
 private final Map<String, Window> windows = new HashMap<>();
 public synchronized void check(String key, int allowed) {
  long now = System.currentTimeMillis();
  windows.entrySet().removeIf(e -> now - e.getValue().start >= 60_000);
  var window = windows.get(key);
  if ((window != null && window.count >= allowed) || (window == null && windows.size() >= 10_000))
   throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many attempts. Please wait a minute and try again.");
  windows.put(key, new Window(window == null ? now : window.start, window == null ? 1 : window.count + 1));
 }
 public void account(String action, String email) {
  // Account-level limits work behind a proxy without trusting client-supplied forwarding headers.
  check(action + ":" + Objects.toString(email, "").trim().toLowerCase(Locale.ROOT), 10);
 }
}
