package com.careerforge.recovery;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;

class BrevoRecoveryMailerTests {
    @Test void sendsResetLinkThroughHttps() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var sender = new BrevoRecoveryMailer("test-key", builder);
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andExpect(header("api-key", "test-key"))
            .andExpect(jsonPath("$.sender.email").value("sender@example.test"))
            .andExpect(jsonPath("$.to[0].email").value("user@example.test"))
            .andExpect(jsonPath("$.textContent").value("https://example.test/reset-password#token=test"))
            .andRespond(withSuccess());
        sender.send(message()); server.verify();
    }
    @Test void providerErrorsDoNotLeakResponseContent() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        var sender = new BrevoRecoveryMailer("test-key", builder);
        server.expect(requestTo("https://api.brevo.com/v3/smtp/email"))
            .andRespond(withBadRequest().body("private provider response"));
        var error = assertThrows(IllegalStateException.class, () -> sender.send(message()));
        assertEquals("Email provider delivery failed", error.getMessage());
        assertNull(error.getCause()); server.verify();
        assertFalse(new BrevoRecoveryMailer("", RestClient.builder()).configured());
    }
    private SimpleMailMessage message() {
        var message = new SimpleMailMessage();
        message.setFrom("sender@example.test"); message.setTo("user@example.test");
        message.setSubject("Reset password"); message.setText("https://example.test/reset-password#token=test");
        return message;
    }
}
