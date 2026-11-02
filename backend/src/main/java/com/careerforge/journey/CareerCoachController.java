package com.careerforge.journey;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/journey/coach")
public class CareerCoachController {
    private final CareerCoachService service;
    public CareerCoachController(CareerCoachService service) { this.service = service; }
    public record Question(String question, boolean consent) {}
    @GetMapping public Object status() { return Map.of("available", service.available(), "provider", service.available() ? "OpenAI" : "Guided coach"); }
    @PostMapping public CareerCoachService.Reply ask(Authentication auth, @RequestBody Question request) { return service.ask((Long) auth.getPrincipal(), request.question(), request.consent()); }
}
