package com.careerforge.controller;

import com.careerforge.dto.CareerGoalResponse;
import com.careerforge.model.CareerGoal;
import com.careerforge.service.CareerGoalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/career-goals")
public class CareerGoalController {

    private final CareerGoalService careerGoalService;

    public CareerGoalController(CareerGoalService careerGoalService) {
        this.careerGoalService = careerGoalService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<CareerGoalResponse> createGoal(
            @PathVariable Long userId,
            @RequestBody CareerGoal careerGoal) {

        CareerGoal savedGoal =
                careerGoalService.createGoal(userId, careerGoal);

        return ResponseEntity.ok(toResponse(savedGoal));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CareerGoalResponse>> getGoals(
            @PathVariable Long userId) {

        List<CareerGoal> goals =
                careerGoalService.getGoals(userId);

        List<CareerGoalResponse> responses =
                goals.stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{goalId}/completion")
    public ResponseEntity<CareerGoalResponse> updateCompletion(
            @PathVariable Long goalId,
            @RequestParam boolean completed) {

        CareerGoal updatedGoal =
                careerGoalService.updateCompletion(
                        goalId,
                        completed
                );

        return ResponseEntity.ok(toResponse(updatedGoal));
    }

    private CareerGoalResponse toResponse(CareerGoal goal) {

        return new CareerGoalResponse(
                goal.getId(),
                goal.getTargetRole(),
                goal.getSkills(),
                goal.getPreferredLocation(),
                goal.getEmploymentType(),
                goal.isCompleted()
        );
    }
}