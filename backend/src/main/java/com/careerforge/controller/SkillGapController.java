package com.careerforge.controller;

import com.careerforge.dto.SkillGapResponse;
import com.careerforge.service.SkillGapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill-gap")

public class SkillGapController {

    private final SkillGapService skillGapService;

    public SkillGapController(
            SkillGapService skillGapService) {

        this.skillGapService = skillGapService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<SkillGapResponse> analyzeSkillGap(
            @PathVariable Long userId,
            @RequestParam("role") String role) {

        return ResponseEntity.ok(
                skillGapService.analyzeSkillGap(
                        userId,
                        role
                )
        );
    }
}
