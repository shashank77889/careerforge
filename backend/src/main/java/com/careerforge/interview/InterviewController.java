package com.careerforge.interview;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private final InterviewService service;
    private final AiMcqGenerator ai;
    public InterviewController(InterviewService service,AiMcqGenerator ai) {this.service=service;this.ai=ai;}
    public record Start(String role, Integer questionCount, Boolean useAI) {}
    public record Answer(int index,String answer,Long version) {}
    public record Finish(Long version) {}
    @GetMapping public Object list(Authentication auth) {return service.list((Long)auth.getPrincipal());}
    @PostMapping public Object start(Authentication auth,@RequestBody Start request) {if(Boolean.TRUE.equals(request.useAI())) {
        if(!Integer.valueOf(30).equals(request.questionCount())) throw new IllegalArgumentException("AI generation is available for 30 MCQs");
        Long user=(Long)auth.getPrincipal();
        return service.startGenerated(user,request.role(),ai.generate(user,request.role()));
    } return service.start((Long)auth.getPrincipal(),request.role(),request.questionCount());}
    @GetMapping("/{id}") public Object get(Authentication auth,@PathVariable Long id) {return service.get((Long)auth.getPrincipal(),id);}
    @PutMapping("/{id}/answer") public Object answer(Authentication auth,@PathVariable Long id,@RequestBody Answer request) {return service.save((Long)auth.getPrincipal(),id,request.index(),request.answer(),request.version());}
    @PostMapping("/{id}/finish") public Object finish(Authentication auth,@PathVariable Long id,@RequestBody Finish request) {return service.finish((Long)auth.getPrincipal(),id,request.version());}
}
