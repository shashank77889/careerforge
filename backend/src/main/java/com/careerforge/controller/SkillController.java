package com.careerforge.controller;

import com.careerforge.model.Skill;
import com.careerforge.service.SkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skills")

public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    // ================= GET USER SKILLS =================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Skill>> getSkills(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                skillService.getSkills(userId)
        );
    }

    // ================= CREATE SKILL =================

    @PostMapping("/user/{userId}")
    public ResponseEntity<Skill> createSkill(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {

        String name = (String) request.get("name");

        Integer proficiency =
                request.get("proficiency") == null
                        ? 0
                        : ((Number) request.get("proficiency"))
                                .intValue();

        Integer confidence =
                request.get("confidence") == null
                        ? 0
                        : ((Number) request.get("confidence"))
                                .intValue();

        Integer targetLevel =
                request.get("targetLevel") == null
                        ? 80
                        : ((Number) request.get("targetLevel"))
                                .intValue();

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Skill skill = skillService.createSkill(
                userId,
                name.trim(),
                proficiency,
                confidence,
                targetLevel
        );

        return ResponseEntity.ok(skill);
    }

    // ================= UPDATE SKILL =================

    @PutMapping("/{skillId}/user/{userId}")
    public ResponseEntity<Skill> updateSkill(
            @PathVariable Long skillId,
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {

        Integer proficiency =
                request.get("proficiency") == null
                        ? null
                        : ((Number) request.get("proficiency"))
                                .intValue();

        Integer confidence =
                request.get("confidence") == null
                        ? null
                        : ((Number) request.get("confidence"))
                                .intValue();

        Integer targetLevel =
                request.get("targetLevel") == null
                        ? null
                        : ((Number) request.get("targetLevel"))
                                .intValue();

        Skill updatedSkill =
                skillService.updateSkill(
                        skillId,
                        userId,
                        proficiency,
                        confidence,
                        targetLevel
                );

        return ResponseEntity.ok(updatedSkill);
    }

    // ================= DELETE SKILL =================

    @DeleteMapping("/{skillId}/user/{userId}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long skillId,
            @PathVariable Long userId) {

        skillService.deleteSkill(
                skillId,
                userId
        );

        return ResponseEntity.noContent().build();
    }

    // ================= REQUIRED SKILLS BY ROLE =================

    @GetMapping("/required-skills")
    public ResponseEntity<Map<String, Integer>> getRequiredSkills(
            @RequestParam String role) {

        return ResponseEntity.ok(
                skillService.getRequiredSkills(role)
        );
    }
}
