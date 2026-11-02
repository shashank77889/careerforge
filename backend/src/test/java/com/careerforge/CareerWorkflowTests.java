package com.careerforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CareerWorkflowTests {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();
    private String token;
    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        if (body != null) builder.header("Content-Type", "application/json");
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }
    private String field(String body, String key) {
        var match = Pattern.compile("\"" + key + "\"\\s*:\\s*(?:\"([^\"]*)\"|(\\d+))").matcher(body);
        assertTrue(match.find(), () -> "Missing " + key + " in " + body);
        return match.group(1) != null ? match.group(1) : match.group(2);
    }
    @Test void accountExportDeletionAndOldTokenRevocation() throws Exception {
        String email="delete-"+System.nanoTime()+"@example.test";
        var registration=request("POST","/users/register","{\"name\":\"Disposable\",\"email\":\""+email+"\",\"password\":\"Test-password-2026\"}");
        String id=field(registration.body(),"id");
        token=field(request("POST","/users/login","{\"email\":\""+email+"\",\"password\":\"Test-password-2026\"}").body(),"token");
        request("POST","/skills/user/"+id,"{\"name\":\"Python\",\"proficiency\":40}");
        request("PUT","/journey","{\"role\":\"SDET\",\"weeklyHours\":5}");
        request("POST","/journey/plan","{}");
        var export=request("GET","/account/export",null);
        assertEquals(200,export.statusCode(),export.body());
        assertTrue(export.body().contains("Python"));
        assertTrue(export.body().contains(email));
        assertFalse(export.body().contains("password"));
        assertFalse(export.body().contains("tokenHash"));
        assertEquals(400,request("DELETE","/account","{\"password\":\"wrong\",\"confirmation\":\"DELETE\"}").statusCode());
        assertEquals(200,request("GET","/journey",null).statusCode());
        assertEquals(204,request("DELETE","/account","{\"password\":\"Test-password-2026\",\"confirmation\":\"DELETE\"}").statusCode());
        assertEquals(401,request("GET","/journey",null).statusCode());
        token=null;
        assertEquals(401,request("POST","/users/login","{\"email\":\""+email+"\",\"password\":\"Test-password-2026\"}").statusCode());
    }
    @Test void excessiveLoginAttemptsAreLimited() throws Exception {
        String email="rate-"+System.nanoTime()+"@example.test";
        for(int i=0;i<10;i++) assertEquals(401,request("POST","/users/login","{\"email\":\""+email+"\",\"password\":\"wrong\"}").statusCode());
        assertEquals(429,request("POST","/users/login","{\"email\":\""+email+"\",\"password\":\"wrong\"}").statusCode());
    }
    @Test void unconfiguredRecoveryIsHonest() throws Exception {
        var response = request("POST", "/users/forgot-password", "{\"email\":\"test@example.test\"}");
        assertEquals(503, response.statusCode());
        assertTrue(response.body().contains("not available"));
    }
    @Test void localFrontendOriginIsAccepted() throws Exception {
        var response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/users/login"))
            .header("Origin", "http://127.0.0.1:5173")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "content-type")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("http://127.0.0.1:5173", response.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
    }
    @Test void anyoneCanRegisterAndSignInWithoutLeakingPasswords() throws Exception {
        String email = "Public.User." + System.nanoTime() + "@Example.Test";
        var registration = request("POST", "/users/register", "{\"name\":\"  Public Student  \",\"email\":\"" + email + "\",\"password\":\"A-strong-test-password\"}");
        assertEquals(200, registration.statusCode(), registration.body());
        assertTrue(registration.body().contains("Public Student"));
        assertFalse(registration.body().toLowerCase().contains("password"));

        var login = request("POST", "/users/login", "{\"email\":\"" + email.toLowerCase() + "\",\"password\":\"A-strong-test-password\"}");
        assertEquals(200, login.statusCode(), login.body());
        assertFalse(field(login.body(), "token").isBlank());
        assertFalse(login.body().toLowerCase().contains("password"));

        assertEquals(401, request("POST", "/users/login", "{\"email\":\"" + email + "\",\"password\":\"wrong-password\"}").statusCode());
        assertEquals(409, request("POST", "/users/register", "{\"name\":\"Duplicate\",\"email\":\"" + email + "\",\"password\":\"A-strong-test-password\"}").statusCode());
        assertEquals(400, request("POST", "/users/register", "{\"name\":\"Short Password\",\"email\":\"short." + System.nanoTime() + "@example.test\",\"password\":\"short\"}").statusCode());
    }
    @Test void careerWorkflowAndAccountIsolation() throws Exception {
        assertEquals(401, request("GET", "/skills/user/1", null).statusCode());
        var user = request("POST", "/users/register", "{\"name\":\"Workflow Test\",\"email\":\"workflow@example.test\",\"password\":\"Test-password-2026\"}");
        assertEquals(200, user.statusCode(), user.body());
        String userId = field(user.body(), "id");
        var login = request("POST", "/users/login", "{\"email\":\"workflow@example.test\",\"password\":\"Test-password-2026\"}");
        assertEquals(200, login.statusCode()); token = field(login.body(), "token");
        var goal = request("POST", "/career-goals/user/" + userId, "{\"targetRole\":\"Build a portfolio\",\"employmentType\":\"Career\"}");
        assertEquals(200, goal.statusCode(), goal.body());
        String goalId = field(goal.body(), "id");
        var completion = request("PUT", "/career-goals/" + goalId + "/completion?completed=true", null);
        assertEquals(200, completion.statusCode()); assertTrue(completion.body().contains("\"completed\":true"));
        var skill = request("POST", "/skills/user/" + userId, "{\"name\":\"Python\",\"proficiency\":40}");
        assertEquals(200, skill.statusCode(), skill.body()); String skillId = field(skill.body(), "id");
        var updated = request("PUT", "/skills/" + skillId + "/user/" + userId, "{\"proficiency\":80}");
        assertEquals(200, updated.statusCode()); assertTrue(updated.body().contains("\"proficiency\":80"));
        var gap = request("GET", "/skill-gap/user/" + userId + "?role=Data%20Scientist", null);
        assertEquals(200, gap.statusCode()); assertTrue(gap.body().contains("missingSkills"));
        assertEquals(200, request("GET", "/learning-recommendations/user/" + userId + "?role=Data%20Scientist", null).statusCode());
        String jobDescription = "Junior software engineer. Build and test web applications using Python, JavaScript, and Spring Boot. "
                + "Collaborate with stakeholders, document APIs, and communicate findings. A bachelor's degree or equivalent project experience is preferred.";
        String resumeText = "Created an accessible portfolio interface with JavaScript and tested form validation. Documented the project and reviewed it with classmates.";
        var match = request("POST", "/job-matches/analyze", "{\"jobTitle\":\"Junior Software Engineer\",\"jobDescription\":\"" + jobDescription + "\",\"resumeText\":\"" + resumeText + "\",\"useSavedResume\":false}");
        assertEquals(200, match.statusCode(), match.body());
        assertTrue(match.body().contains("\"score\":"));
        assertTrue(match.body().contains("\"status\":\"MATCHED\""));
        assertTrue(match.body().contains("\"status\":\"PROFILE_ONLY\""));
        assertTrue(match.body().contains("\"status\":\"MISSING\""));
        assertTrue(match.body().contains("Do not add this as a claimed skill yet"));
        String matchId = field(match.body(), "id");
        assertTrue(request("GET", "/job-matches", null).body().contains("Junior Software Engineer"));
        assertEquals(200, request("GET", "/job-matches/" + matchId, null).statusCode());
        var application = request("POST", "/applications", "{\"job\":{\"title\":\"Developer\",\"company\":\"Test Company\"},\"status\":\"Applied\"}");
        assertEquals(200, application.statusCode(), application.body()); String applicationId = field(application.body(), "id");
        var interview = request("PUT", "/applications/" + applicationId, "{\"status\":\"Interview\"}");
        assertEquals(200, interview.statusCode()); assertTrue(interview.body().contains("\"status\":\"Interview\""));
        assertEquals(400, request("POST", "/applications", "{\"job\":{\"title\":\"Bad status\",\"company\":\"Test\"},\"status\":\"Invalid\"}").statusCode());
        assertEquals(204, request("DELETE", "/skills/" + skillId + "/user/" + userId, null).statusCode());
        assertEquals("[]", request("GET", "/skills/user/" + userId, null).body());
        byte[] resume;
        try (var doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(); var output = new java.io.ByteArrayOutputStream()) {
            doc.createParagraph().createRun().setText("Test Resume. Skills: Python, Java, SQL, React, Git.");
            doc.write(output); resume = output.toByteArray();
        }
        String boundary = "CareerForgeTestBoundary";
        var multipart = HttpRequest.BodyPublishers.concat(
            HttpRequest.BodyPublishers.ofString("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"test-resume.docx\"\r\nContent-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document\r\n\r\n"),
            HttpRequest.BodyPublishers.ofByteArray(resume),
            HttpRequest.BodyPublishers.ofString("\r\n--" + boundary + "--\r\n"));
        var upload = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/resumes/user/" + userId))
            .header("Authorization", "Bearer " + token).header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(multipart).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, upload.statusCode(), upload.body());
        assertTrue(request("GET", "/skills/user/" + userId, null).body().contains("Python"));
        request("POST", "/users/register", "{\"name\":\"Other Test\",\"email\":\"other@example.test\",\"password\":\"Test-password-2026\"}");
        token = field(request("POST", "/users/login", "{\"email\":\"other@example.test\",\"password\":\"Test-password-2026\"}").body(), "token");
        assertEquals(403, request("GET", "/skills/user/" + userId, null).statusCode());
        assertEquals(403, request("PUT", "/career-goals/" + goalId + "/completion?completed=false", null).statusCode());
        assertEquals(403, request("PUT", "/applications/" + applicationId, "{\"status\":\"Selected\"}").statusCode());
        assertEquals(404, request("GET", "/job-matches/" + matchId, null).statusCode());
    }
}



