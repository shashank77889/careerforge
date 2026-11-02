package com.careerforge;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class PasswordRecoveryMigrationTests {
    @Test void migrationPreservesRevocationStateWhenReapplied() throws Exception {
        try (var connection = java.sql.DriverManager.getConnection("jdbc:h2:mem:recovery-migration;MODE=MySQL", "sa", "")) {
            var script = new ClassPathResource("db/2026-09-password-recovery.sql");
            ScriptUtils.executeSqlScript(connection, script);
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO password_recovery(user_id, session_version) VALUES (1, 4)");
                ScriptUtils.executeSqlScript(connection, script);
                try (var rows = statement.executeQuery("SELECT session_version FROM password_recovery WHERE user_id = 1")) {
                    assertTrue(rows.next()); assertEquals(4, rows.getLong(1));
                }
            }
        }
    }
    @Test void recoveryRequestsAreRateLimited() {
        var service = org.mockito.Mockito.mock(com.careerforge.recovery.PasswordRecoveryService.class);
        var controller = new com.careerforge.recovery.PasswordRecoveryController(service);
        var request = new org.springframework.mock.web.MockHttpServletRequest();
        var input = new com.careerforge.recovery.PasswordRecoveryController.Forgot("test@example.test");
        for (int index = 0; index < 20; index++) controller.forgot(input, request);
        var error = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.forgot(input, request));
        assertEquals(429, error.getStatusCode().value());
        org.mockito.Mockito.verify(service, org.mockito.Mockito.times(20)).request("test@example.test");
    }
}
