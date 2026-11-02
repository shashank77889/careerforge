package com.careerforge.jobmatch;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/job-matches")
public class JobMatchController {
    private final JobMatchService service;
    public JobMatchController(JobMatchService service) { this.service = service; }

    @GetMapping
    public List<JobMatchService.Summary> list(Authentication authentication) {
        return service.list(userId(authentication));
    }

    @GetMapping("/{id}")
    public JobMatchService.View get(Authentication authentication, @PathVariable Long id) {
        return service.get(userId(authentication), id);
    }

    @PostMapping("/analyze")
    public JobMatchService.View analyze(Authentication authentication, @RequestBody JobMatchService.AnalyzeRequest request) {
        return service.analyzeText(userId(authentication), request);
    }

    @PostMapping(value = "/analyze-file", consumes = "multipart/form-data")
    public JobMatchService.View analyzeFile(Authentication authentication,
                                             @RequestParam(required = false) String jobTitle,
                                             @RequestParam String jobDescription,
                                             @RequestParam("resume") MultipartFile resume) throws IOException {
        return service.analyzeFile(userId(authentication), jobTitle, jobDescription, resume);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        service.delete(userId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    private Long userId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long id))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Please sign in again");
        return id;
    }
}
