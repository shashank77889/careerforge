package com.careerforge.interview;
import java.util.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.json.JsonMapper;
import com.careerforge.journey.JourneyService;

@Service
public class AiMcqGenerator {
    private final String key, model;
    private final JsonMapper json=JsonMapper.builder().build();
    private final HttpClient client=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(4)).build();
    private final Map<Long,Long> requested=new HashMap<>();
    public AiMcqGenerator(@Value("${careerforge.ai.api-key:}") String key,@Value("${careerforge.ai.model:}") String model){this.key=key;this.model=model;}
    public List<InterviewQuestions.Question> generate(Long user,String role) {
        if(!JourneyService.ROLES.contains(role)) throw new IllegalArgumentException("Choose a supported role");
        if(key.isBlank()||model.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"AI is not configured. Choose curated MCQs for now.");
        synchronized(requested){
            long now=System.currentTimeMillis();requested.entrySet().removeIf(e->now-e.getValue()>60000);
            if(requested.containsKey(user)||requested.size()>=1000) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Wait one minute before generating another AI interview.");
            requested.put(user,now);
        }
        try {
            var item=Map.of("type","object","additionalProperties",false,"required",List.of("topic","prompt","options","correctIndex","explanation"),
                "properties",Map.of("topic",Map.of("type","string"),"prompt",Map.of("type","string"),"options",Map.of("type","array","items",Map.of("type","string")),
                "correctIndex",Map.of("type","integer"),"explanation",Map.of("type","string")));
            var schema=Map.of("type","object","additionalProperties",false,"required",List.of("questions"),
                "properties",Map.of("questions",Map.of("type","array","items",item)));
            var body=Map.of("model",model,"store",false,"max_output_tokens",6500,
                "text",Map.of("format",Map.of("type","json_schema","name","interview_mcqs","strict",true,"schema",schema)),
                "instructions","Create original entry-level interview practice. Return exactly 30 unique MCQs: 20 technical questions relevant to the role and 10 quantitative or logical aptitude questions (topic begins Aptitude). Four distinct options each, one unambiguously correct, correctIndex 0 to 3. Include a concise worked explanation, calculation when relevant, and why the distractors fail. Use common concepts, never claim these are actual company questions. Vary scenarios and numerical inputs. No sensitive inferences, URLs, tools, or external actions. Verify your arithmetic. Keep explanations under 45 words.",
                "input","Role: "+role+". Variation seed: "+UUID.randomUUID());
            var request=HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/responses")).timeout(Duration.ofSeconds(16))
                .header("Authorization","Bearer "+key).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build();
            var response=client.send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode()!=200) throw new IllegalStateException();
            var tree=json.readTree(response.body());
            if(!"completed".equals(tree.path("status").asText())) throw new IllegalStateException();
            var text=new StringBuilder();
            for(var output:tree.path("output")) for(var content:output.path("content")) if("output_text".equals(content.path("type").asText()))text.append(content.path("text").asText());
            return validate(json.readValue(json.readTree(text.toString()).path("questions").toString(),InterviewQuestions.Question[].class));
        } catch(InterruptedException e){Thread.currentThread().interrupt();throw unavailable();}
        catch(Exception e){throw unavailable();}
    }
    static List<InterviewQuestions.Question> validate(InterviewQuestions.Question[] questions) {
        if(questions.length!=30)throw new IllegalArgumentException("Expected 30 questions");
        var prompts=new HashSet<String>();var result=new ArrayList<InterviewQuestions.Question>();int aptitude=0;
        for(var q:questions) {
            if(q.topic()==null||q.topic().isBlank()||q.topic().length()>100||q.prompt()==null||q.prompt().isBlank()||q.prompt().length()>1000
                ||!prompts.add(q.prompt().trim().toLowerCase(Locale.ROOT))||q.options()==null||q.options().size()!=4
                ||q.options().stream().anyMatch(o->o==null||o.isBlank()||o.length()>500)||new HashSet<>(q.options()).size()!=4
                ||q.correctIndex()==null||q.correctIndex()<0||q.correctIndex()>3||q.explanation()==null||q.explanation().isBlank()||q.explanation().length()>2000)
                throw new IllegalArgumentException("Invalid question");
            if(q.topic().startsWith("Aptitude"))aptitude++;
            var options=new ArrayList<>(q.options());Collections.shuffle(options);
            result.add(new InterviewQuestions.Question("AI · "+q.topic(),q.prompt(),List.of(),options,options.indexOf(q.options().get(q.correctIndex())),q.explanation()));
        }
        if(aptitude!=10)throw new IllegalArgumentException("Expected 10 aptitude questions");
        Collections.shuffle(result);return result;
    }
    private ResponseStatusException unavailable(){return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"AI could not prepare a complete interview. Please try again later or use curated MCQs.");}
}
