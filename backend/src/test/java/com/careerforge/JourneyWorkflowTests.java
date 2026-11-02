package com.careerforge;

import com.careerforge.journey.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;
import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JourneyWorkflowTests {
    @LocalServerPort int port;
    final HttpClient client = HttpClient.newHttpClient();
    final JsonMapper json = JsonMapper.builder().build();
    String token;
    long userId;
    HttpResponse<String> call(String method, String path, Object body) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api" + path));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        if (body != null) builder.header("Content-Type", "application/json");
        return client.send(builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(), HttpResponse.BodyHandlers.ofString());
    }
    JsonNode ok(String method, String path, Object body) throws Exception {
        var response = call(method,path,body);
        assertEquals(200,response.statusCode(),response.body());
        return json.readTree(response.body());
    }
    void newUser() throws Exception {
        token = null;
        String email = UUID.randomUUID() + "@example.test";
        var user = ok("POST","/users/register",Map.of("name","Journey Test","email",email,"password","Test-password-2026"));
        userId = user.path("id").asLong();
        token = ok("POST","/users/login",Map.of("email",email,"password","Test-password-2026")).path("token").asText();
    }
    @BeforeEach void setup() throws Exception { newUser(); }

    @Test void readinessPlanAndProgressStayConnectedWithoutInflatingSkillRatings() throws Exception {
        var skill = ok("POST","/skills/user/"+userId,Map.of("name","Python","proficiency",20));
        var initial = ok("PUT","/journey",Map.of("role","Data Scientist","weeklyHours",5));
        assertTrue(initial.path("configured").asBoolean());
        int initialScore = initial.path("readiness").path("score").asInt();
        assertTrue(initialScore > 0 && initialScore < 20);
        ok("PUT","/skills/"+skill.path("id").asLong()+"/user/"+userId,Map.of("proficiency",100));
        var improved = ok("GET","/journey",null);
        int score = improved.path("readiness").path("score").asInt();
        assertTrue(score > initialScore);
        assertEquals(1,improved.path("readiness").path("matched").asInt());
        var plan = ok("POST","/journey/plan",null);
        assertEquals(7,plan.path("total").asInt());
        var scheduled = ok("PUT","/journey/schedule",Map.of("weeklyHours",6,"days",List.of("TUESDAY","THURSDAY","SATURDAY"),"startTime","18:30","sessionMinutes",60,"focusSkills",List.of("Statistics","Pandas")));
        assertEquals(6,scheduled.path("weeklyHours").asInt());
        assertEquals("18:30",scheduled.path("studyPreferences").path("startTime").asText());
        assertEquals(3,scheduled.path("studyPreferences").path("days").size());
        assertEquals(2,scheduled.path("studyPreferences").path("focusSkills").size());
        assertEquals(400,call("PUT","/journey/schedule",Map.of("weeklyHours",6,"days",List.of(),"startTime","99:30","sessionMinutes",17,"focusSkills",List.of())).statusCode());
        var names = new HashSet<String>();
        for (var step : plan.path("plan")) {
            assertTrue(names.add(step.path("skill").asText()));
            assertNotEquals("Python",step.path("skill").asText());
            assertTrue(step.path("resource").path("url").asText().startsWith("https://"));
        }
        assertTrue(names.containsAll(List.of("Statistics","Pandas","NumPy","Data Visualization","Scikit-learn")));
        long id = plan.path("plan").get(0).path("id").asLong();
        ok("PUT","/journey/plan/"+id,Map.of("status","IN_PROGRESS","reflection",""));
        assertEquals(400,call("PUT","/journey/plan/"+id,Map.of("status","COMPLETED","reflection","")).statusCode());
        var completed = ok("PUT","/journey/plan/"+id,Map.of("status","COMPLETED","reflection","Built an example and checked an edge case."));
        assertEquals(1,completed.path("completed").asInt());
        assertEquals(score,completed.path("readiness").path("score").asInt());
        var refreshed = ok("POST","/journey/plan",null);
        assertEquals(7,refreshed.path("total").asInt());
        assertEquals(1,refreshed.path("completed").asInt());
        assertEquals("Built an example and checked an edge case.", refreshed.path("plan").get(0).path("reflection").asText());
        ok("POST","/journey/reassess",null);
        assertEquals(1,ok("GET","/journey",null).path("history").size());
        ok("PUT","/journey",Map.of("role","Frontend Developer","weeklyHours",10));
        assertEquals(0,ok("GET","/journey",null).path("total").asInt());
        ok("PUT","/journey",Map.of("role","Data Scientist","weeklyHours",5));
        assertEquals(1,ok("GET","/journey",null).path("completed").asInt());
        newUser();
        assertEquals(0,ok("GET","/journey",null).path("total").asInt());
        assertEquals(404,call("PUT","/journey/plan/"+id,Map.of("status","COMPLETED","reflection","Intrusion")).statusCode());
    }
    @Test void interviewsResumeWithAccountIsolationAndConflictProtection() throws Exception {
        assertEquals(200,call("PUT","/journey/activity",Map.of("path","/interviews")).statusCode());
        assertEquals("/interviews",ok("GET","/journey",null).path("lastPage").asText());
        assertEquals(400,call("PUT","/journey/activity",Map.of("path","https://other.test")).statusCode());
        var session = ok("POST","/interviews",Map.of("role","Frontend Developer","questionCount",5));
        long id = session.path("id").asLong();
        assertEquals(5,session.path("questions").size());
        assertEquals(400,call("POST","/interviews/"+id+"/finish",Map.of("version",session.path("version").asLong())).statusCode());
        long initialVersion = session.path("version").asLong();
        for (int index = 0; index < 5; index++) {
            session = ok("PUT","/interviews/"+id+"/answer",Map.of("index",index,"answer","My practical answer " + index,"version",session.path("version").asLong()));
        }
        assertEquals(409,call("PUT","/interviews/"+id+"/answer",Map.of("index",0,"answer","Stale answer","version",initialVersion)).statusCode());
        var resumed = ok("GET","/interviews/"+id,null);
        assertEquals("My practical answer 0",resumed.path("answers").get(0).asText());
        assertEquals(4,resumed.path("currentQuestion").asInt());
        assertEquals(5,ok("GET","/interviews",null).get(0).path("answered").asInt());
        session = ok("POST","/interviews/"+id+"/finish",Map.of("version",resumed.path("version").asLong()));
        assertEquals("COMPLETED",session.path("status").asText());
        assertEquals(400,call("PUT","/interviews/"+id+"/answer",Map.of("index",0,"answer","Changed","version",session.path("version").asLong())).statusCode());
        assertEquals(400,call("POST","/interviews",Map.of("role","Unsupported")).statusCode());
        newUser();
        assertEquals(404,call("GET","/interviews/"+id,null).statusCode());
        assertEquals(404,call("PUT","/interviews/"+id+"/answer",Map.of("index",0,"answer","Intrusion","version",session.path("version").asLong())).statusCode());
        assertEquals(0,ok("GET","/interviews",null).size());
        for (String role : JourneyService.ROLES) assertEquals(10,ok("POST","/interviews",Map.of("role",role)).path("questions").size());
        var full = ok("POST","/interviews",Map.of("role","Frontend Developer","questionCount",20));
        assertEquals(20,full.path("questions").size());
        assertEquals(20,full.path("answers").size());
        assertEquals(full.path("questions"),ok("GET","/interviews/"+full.path("id").asLong(),null).path("questions"));
        for (int count : new int[]{0, -1, 6, 1000}) {
            assertEquals(400,call("POST","/interviews",Map.of("role","Frontend Developer","questionCount",count)).statusCode());
        }
    }
    @Test void validationAndLocalCoachDoNotRequireAnAiKey() throws Exception {
        assertEquals(400,call("PUT","/journey",Map.of("role","Data Scientist","weeklyHours",100)).statusCode());
        assertEquals(400,call("PUT","/journey",Map.of("role","Unknown role","weeklyHours",5)).statusCode());
        assertEquals(400,call("POST","/skills/user/"+userId,Map.of("name","Python","proficiency",200)).statusCode());
        ok("POST","/skills/user/"+userId,Map.of("name","Python","proficiency",40));
        assertEquals(400,call("POST","/skills/user/"+userId,Map.of("name","python","proficiency",50)).statusCode());
        var reply = ok("POST","/journey/coach",Map.of("question","Suggest a portfolio project","consent",false));
        assertEquals("GUIDED",reply.path("mode").asText());
        assertTrue(reply.path("answer").asText().contains("Data Scientist"));
        var callPrep = ok("POST","/journey/coach",Map.of("question","A senior employee has my resume and will call me. What questions can they ask?", "consent",false));
        assertTrue(callPrep.path("answer").asText().contains("WHEN THE PHONE RINGS"));
        assertTrue(callPrep.path("answer").asText().contains("QUESTIONS TO EXPECT FOR DATA SCIENTIST"));
        assertTrue(callPrep.path("answer").asText().contains("readiness percentage private"));
        assertFalse(callPrep.path("answer").asText().contains("Reserve 5 hours this week"));
        var today = LocalDate.now();
        var study = ok("PUT","/journey/study-log",Map.of("sessionKey","MONDAY-19:00-Python-0","date",today,"skill","Python","plannedMinutes",60,"studiedMinutes",48,"completed",true));
        assertEquals(48,study.path("studiedMinutes").asInt());
        assertTrue(study.path("completed").asBoolean());
        var studyList = ok("GET","/journey/study-log?from="+today.minusDays(3)+"&to="+today.plusDays(1),null);
        assertEquals(1,studyList.size());
        assertEquals(400,call("PUT","/journey/study-log",Map.of("sessionKey","bad","date",today,"skill","Python","plannedMinutes",60,"studiedMinutes",900,"completed",true)).statusCode());
        var match = ok("POST","/journey/compare-job",Map.of("description","We use Python, Pandas, and JavaScript."));
        assertEquals(3,match.path("detected").size());
        assertEquals(1,match.path("matched").size());
        assertEquals(2,match.path("missing").size());
        var project = ok("GET","/journey/portfolio-project",null);
        assertEquals("Data Scientist",project.path("role").asText());
        assertTrue(project.path("skills").size() > 0);
        assertEquals(4,project.path("proofChecklist").size());
        assertTrue(project.path("resumeBulletTemplate").asText().contains("[test or measurable result]"));
    }
    @Test void everyRoleHasResourcesAndWholeWordDetectionAvoidsFalsePositives() throws Exception {
        var catalog = new LearningCatalog();
        assertFalse(LearningCatalog.mentions("JavaScript development","Java"));
        assertFalse(LearningCatalog.mentions("Contact information","C"));
        assertTrue(LearningCatalog.mentions("Used Java, Python and C++.","C++"));
        for (String role : JourneyService.ROLES) {
            var overview = ok("PUT","/journey",Map.of("role",role,"weeklyHours",5));
            for (var level : overview.path("readiness").path("skills")) {
                var learning = catalog.get(level.path("skill").asText());
                assertNotNull(learning);
                assertTrue(learning.reviewed());
                assertFalse(learning.provider().isBlank());
            }
            var plan = ok("POST","/journey/plan",null);
            assertTrue(plan.path("total").asInt() > 0);
            assertTrue(plan.path("plan").get(0).path("resource").path("reviewed").asBoolean());
            assertFalse(plan.path("plan").get(0).path("resource").path("provider").asText().isBlank());
            assertEquals(role,ok("GET","/journey/portfolio-project",null).path("role").asText());
        }
    }

    @Test void mcqAnswersStayHiddenUntilFinishAndScoresAreSaved() throws Exception {
        for (String role : JourneyService.ROLES) {
            var run=ok("POST","/interviews",Map.of("role",role,"questionCount",30));
            assertEquals(30,run.path("questions").size());
            for(var question:run.path("questions")) {
                assertEquals(4,question.path("options").size());
                assertTrue(question.path("correctIndex").isNull());
                assertTrue(question.path("explanation").isNull());
            }
        }
        var run=ok("POST","/interviews",Map.of("role","SDET","questionCount",30));
        long id=run.path("id").asLong();
        assertEquals(400,call("PUT","/interviews/"+id+"/answer",Map.of("index",0,"answer","4","version",run.path("version").asLong())).statusCode());
        assertEquals(400,call("POST","/interviews/"+id+"/finish",Map.of("version",run.path("version").asLong())).statusCode());
        for(int i=0;i<30;i++) run=ok("PUT","/interviews/"+id+"/answer",Map.of("index",i,"answer","0","version",run.path("version").asLong()));
        assertTrue(run.path("score").isNull());
        run=ok("POST","/interviews/"+id+"/finish",Map.of("version",run.path("version").asLong()));
        int correct=0;
        for(var question:run.path("questions")) {
            assertFalse(question.path("explanation").asText().isBlank());
            if(question.path("correctIndex").asInt()==0)correct++;
        }
        assertEquals(correct,run.path("score").asInt());
        var resumed=ok("GET","/interviews/"+id,null); assertEquals(run.path("questions"),resumed.path("questions")); assertEquals(run.path("answers"),resumed.path("answers")); assertEquals(run.path("score"),resumed.path("score"));
        newUser();
        assertEquals(404,call("GET","/interviews/"+id,null).statusCode());
    }
}

