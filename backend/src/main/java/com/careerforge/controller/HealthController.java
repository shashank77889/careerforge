package com.careerforge.controller;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final org.springframework.jdbc.core.JdbcTemplate database;
    public HealthController(org.springframework.jdbc.core.JdbcTemplate database) { this.database = database; }
    @GetMapping
    public Map<String, Object> health() {
        try { database.queryForObject("SELECT 1", Integer.class); }
        catch (org.springframework.dao.DataAccessException exception) { throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE, "Service is temporarily unavailable"); }
        return Map.of("status", "UP", "service", "careerforge-api", "time", Instant.now());
    }
}
