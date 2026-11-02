package com.careerforge.journey;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/journey")
public class JourneyController {
    private final JourneyService service;
    private final StudyLogService studyLogs;
    public JourneyController(JourneyService service, StudyLogService studyLogs) { this.service = service; this.studyLogs = studyLogs; }
    public record Preferences(@NotBlank String role, @Min(1) @Max(40) int weeklyHours) {}
    public record StudySchedule(@Min(1) @Max(40) int weeklyHours, @NotEmpty @Size(max = 7) List<String> days,
                                @NotBlank @Pattern(regexp = "(?:[01]\\d|2[0-3]):[0-5]\\d") String startTime,
                                @Min(30) @Max(120) int sessionMinutes, @NotNull @Size(max = 8) List<String> focusSkills) {}
    public record StudyEntry(@NotBlank @Size(max = 180) String sessionKey, @NotNull LocalDate date, @NotBlank String skill,
                             @Min(1) @Max(240) int plannedMinutes, @Min(0) @Max(720) int studiedMinutes, boolean completed) {}
    public record StepUpdate(@NotBlank String status, @NotNull @Size(max = 2000) String reflection) {}
    public record JobText(@NotBlank @Size(max = 15000) String description) {}
    private Long user(Authentication authentication) { return (Long) authentication.getPrincipal(); }
    public record Activity(String path) {}
    @PutMapping("/activity") public void activity(Authentication auth, @RequestBody Activity request) { service.rememberPage(user(auth), request.path()); }
    @GetMapping public JourneyService.Overview get(Authentication auth) { return service.overview(user(auth)); }
    @PutMapping public JourneyService.Overview configure(Authentication auth, @Valid @RequestBody Preferences request) { return service.configure(user(auth), request.role(), request.weeklyHours()); }
    @PutMapping("/schedule") public JourneyService.Overview schedule(Authentication auth, @Valid @RequestBody StudySchedule request) { return service.configureSchedule(user(auth), request.weeklyHours(), request.days(), request.startTime(), request.sessionMinutes(), request.focusSkills()); }
    @GetMapping("/study-log") public Object studyLog(Authentication auth, @RequestParam LocalDate from, @RequestParam LocalDate to) { return studyLogs.list(user(auth), from, to); }
    @PutMapping("/study-log") public Object studyLog(Authentication auth, @Valid @RequestBody StudyEntry request) { return studyLogs.save(user(auth), request.sessionKey(), request.date(), request.skill(), request.plannedMinutes(), request.studiedMinutes(), request.completed()); }
    @PostMapping("/plan") public JourneyService.Overview generate(Authentication auth) { return service.generate(user(auth)); }
    @PutMapping("/plan/{id}") public JourneyService.Overview update(Authentication auth, @PathVariable Long id, @Valid @RequestBody StepUpdate request) { return service.updateStep(user(auth), id, request.status(), request.reflection()); }
    @PostMapping("/reassess") public JourneyService.Overview reassess(Authentication auth) { return service.reassess(user(auth)); }
    @GetMapping("/resume-evidence") public Object evidence(Authentication auth) { return service.resumeEvidence(user(auth)); }
    @PostMapping("/compare-job") public Object compare(Authentication auth, @Valid @RequestBody JobText request) { return service.compareJob(user(auth), request.description()); }
    @GetMapping("/portfolio-project") public Object project(Authentication auth) { return service.portfolioProject(user(auth)); }
}

