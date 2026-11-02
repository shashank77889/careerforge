package com.careerforge.controller;

import com.careerforge.model.Job;
import com.careerforge.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Job>> getRecommendations(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                recommendationService.getRecommendations(userId)
        );
    }
}