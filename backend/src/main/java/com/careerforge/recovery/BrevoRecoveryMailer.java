package com.careerforge.recovery;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BrevoRecoveryMailer {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BrevoRecoveryMailer.class);
    private final String key;
    private final RestClient client;

    @org.springframework.beans.factory.annotation.Autowired
    public BrevoRecoveryMailer(@Value("${careerforge.recovery.brevo-api-key:}") String key) {
        this(key, builder());
    }
    BrevoRecoveryMailer(String key, RestClient.Builder builder) {
        this.key = key;
        this.client = builder.baseUrl("https://api.brevo.com/v3").build();
    }
    private static RestClient.Builder builder() {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var factory = new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(factory);
    }
    public boolean configured() { return !key.isBlank(); }
    public void send(SimpleMailMessage message) {
        if (!configured()) throw new IllegalStateException("Email provider is not configured");
        try {
            var response = client.post().uri("/smtp/email").header("api-key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("sender", Map.of("name", "CareerForge", "email", message.getFrom()),
                    "to", List.of(Map.of("email", message.getTo()[0])),
                    "subject", message.getSubject(), "textContent", message.getText()))
                .retrieve().body(Map.class);
            if (response != null && response.get("messageId") instanceof String id && id.matches("[A-Za-z0-9@._<>-]{1,200}"))
                log.info("Password recovery: Brevo accepted message ID {}. Check this ID in transactional delivery logs.", id);
        } catch (org.springframework.web.client.RestClientResponseException exception) {
            log.error("Password recovery: Brevo rejected delivery with HTTP {}. Check API key, verified sender, account activation and quota.", exception.getStatusCode().value());
            throw new IllegalStateException("Email provider delivery failed");
        } catch (Exception exception) {
            log.error("Password recovery: Brevo transport or request failure; check outbound HTTPS availability.");
            // Provider responses may include private content. Do not propagate them to logs.
            throw new IllegalStateException("Email provider delivery failed");
        }
    }
}
