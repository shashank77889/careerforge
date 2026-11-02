package com.careerforge;

import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class JobMatchMigrationTests {
    @Test void releaseMigrationCanRunAgainWithoutLosingSavedAnalyses() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:jobmatch-migration;MODE=MySQL", "sa", "")) {
            var resource = new ClassPathResource("db/2026-09-job-match.sql");
            ScriptUtils.executeSqlScript(connection, resource);
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO job_match_analyses (user_id,job_title,job_description,resume_label,score,result_json,created_at,updated_at) VALUES (1,'Test role','Test description','Pasted text',50,'{}',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)");
                ScriptUtils.executeSqlScript(connection, resource);
                try (var rows = statement.executeQuery("SELECT score, resume_label FROM job_match_analyses")) {
                    assertTrue(rows.next());
                    assertEquals(50, rows.getInt("score"));
                    assertEquals("Pasted text", rows.getString("resume_label"));
                    assertFalse(rows.next());
                }
            }
        }
    }
}
