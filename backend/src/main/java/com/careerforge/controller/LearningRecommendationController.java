package com.careerforge.controller;

import com.careerforge.dto.LearningRecommendationResponse;
import com.careerforge.service.LearningRecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-recommendations")
public class LearningRecommendationController {

    private final LearningRecommendationService learningRecommendationService;

    public LearningRecommendationController(
            LearningRecommendationService learningRecommendationService) {
        this.learningRecommendationService = learningRecommendationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LearningRecommendationResponse>> getRecommendations(
            @PathVariable Long userId, @RequestParam(defaultValue = "Data Scientist") String role) {

        return ResponseEntity.ok(
                learningRecommendationService.getRecommendations(userId, role)
        );
    }
}
