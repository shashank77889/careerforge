package com.careerforge;

import com.careerforge.recovery.*;
import com.careerforge.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import java.net.*;
import java.net.http.*;
import java.time.Instant;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "careerforge.recovery.enabled=true", "careerforge.recovery.from=noreply@example.test",
    "careerforge.recovery.frontend-url=https://careerforge.example.test"
})
class PasswordRecoveryTests {
    @LocalServerPort int port;
    @MockitoBean JavaMailSender mail;
    @Autowired UserRepository users;
    @Autowired PasswordRecoveryRepository recoveries;
    final BlockingQueue<SimpleMailMessage> inbox = new LinkedBlockingQueue<>();
    final HttpClient client = HttpClient.newHttpClient();
    @BeforeEach void captureMail() { doAnswer(call -> { inbox.add(call.getArgument(0)); return null; }).when(mail).send(any(SimpleMailMessage.class)); }
    HttpResponse<String> post(String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users/" + path))
            .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    String field(String json, String key) { var matcher = java.util.regex.Pattern.compile("\"" + key + "\":\"([^\"]+)\"").matcher(json); assertTrue(matcher.find(), json); return matcher.group(1); }
    String register(String email) throws Exception {
        assertEquals(200, post("register", "{\"name\":\"Recovery Test\",\"email\":\"" + email + "\",\"password\":\"Original-password1\"}").statusCode());
        return field(post("login", "{\"email\":\"" + email + "\",\"password\":\"Original-password1\"}").body(), "token");
    }
    String requestToken(String email) throws Exception {
        assertEquals(200, post("forgot-password", "{\"email\":\"" + email + "\"}").statusCode());
        var message = inbox.poll(5, TimeUnit.SECONDS);
        assertNotNull(message);
        assertEquals("noreply@example.test", message.getFrom());
        return message.getText().split("#token=")[1].split("\\s")[0];
    }
    String resetBody(String token) { return "{\"token\":\"" + token + "\",\"password\":\"Replacement-password2\"}"; }

    @Test void resetIsSingleUseRevokesSessionsAndKeepsAccount() throws Exception {
        String email = "reset." + System.nanoTime() + "@example.test";
        String oldJwt = register(email);
        Long id = users.findByEmail(email).orElseThrow().getId();
        var configure = client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/journey"))
            .header("Authorization","Bearer "+oldJwt).header("Content-Type","application/json")
            .PUT(HttpRequest.BodyPublishers.ofString("{\"role\":\"Java Developer\",\"weeklyHours\":6}")).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,configure.statusCode());
        var plan = client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/journey/plan"))
            .header("Authorization","Bearer "+oldJwt).POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(200,plan.statusCode());
        String token = requestToken(email.toUpperCase());
        assertNotEquals(token, recoveries.findById(id).orElseThrow().tokenHash);
        assertEquals(400, post("reset-password", "{\"token\":\"" + token + "\",\"password\":\"short\"}").statusCode());
        assertEquals(200, post("reset-password", resetBody(token)).statusCode());
        assertEquals(400, post("reset-password", resetBody(token)).statusCode());
        assertEquals(401, post("login", "{\"email\":\"" + email + "\",\"password\":\"Original-password1\"}").statusCode());
        assertEquals(200, post("login", "{\"email\":\"" + email + "\",\"password\":\"Replacement-password2\"}").statusCode());
        assertEquals(id, users.findByEmail(email).orElseThrow().getId());
        String newJwt=field(post("login","{\"email\":\""+email+"\",\"password\":\"Replacement-password2\"}").body(),"token");
        var restored=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/journey"))
            .header("Authorization","Bearer "+newJwt).GET().build(),HttpResponse.BodyHandlers.ofString());
        var mapper=tools.jackson.databind.json.JsonMapper.builder().build();
        assertEquals("Java Developer",mapper.readTree(restored.body()).path("role").asText());
        assertEquals(6,mapper.readTree(restored.body()).path("weeklyHours").asInt());
        assertEquals(mapper.readTree(plan.body()).path("plan"),mapper.readTree(restored.body()).path("plan"));
        var protectedResponse = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/journey"))
            .header("Authorization", "Bearer " + oldJwt).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(401, protectedResponse.statusCode());
    }
    @Test void expiredTokenDoesNotChangePasswordAndRequestsStayGeneric() throws Exception {
        String email = "expired." + System.nanoTime() + "@example.test";
        register(email);
        String token = requestToken(email);
        var known = post("forgot-password", "{\"email\":\"" + email + "\"}");
        var unknown = post("forgot-password", "{\"email\":\"absent@example.test\"}");
        assertEquals(known.statusCode(), unknown.statusCode()); assertEquals(known.body(), unknown.body());
        assertNull(inbox.poll(200, TimeUnit.MILLISECONDS));
        var state = recoveries.findById(users.findByEmail(email).orElseThrow().getId()).orElseThrow();
        state.expiresAt = Instant.now().minusSeconds(1); recoveries.save(state);
        assertEquals(400, post("reset-password", resetBody(token)).statusCode());
        assertEquals(200, post("login", "{\"email\":\"" + email + "\",\"password\":\"Original-password1\"}").statusCode());
    }
    @Test void concurrentRedemptionHasOnlyOneWinner() throws Exception {
        String email = "concurrent." + System.nanoTime() + "@example.test";
        register(email); String token = requestToken(email);
        try (var pool = Executors.newFixedThreadPool(2)) {
            var first = pool.submit(() -> post("reset-password", resetBody(token)).statusCode());
            var second = pool.submit(() -> post("reset-password", resetBody(token)).statusCode());
            var results = new java.util.HashSet<>(java.util.List.of(first.get(), second.get()));
            assertEquals(java.util.Set.of(200, 400), results);
        }
    }
}
