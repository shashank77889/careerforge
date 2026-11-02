package com.careerforge.controller;

import com.careerforge.dto.DashboardResponse;
import com.careerforge.dto.ProfileResponse;
import com.careerforge.model.Profile;
import com.careerforge.model.User;
import com.careerforge.service.ProfileService;
import com.careerforge.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final UserRepository userRepository;
    private final ProfileService profileService;

    public DashboardController(
            UserRepository userRepository,
            ProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<DashboardResponse> getDashboard(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileService.getProfile(userId);

        ProfileResponse profileResponse = new ProfileResponse(
                profile.getId(),
                profile.getEducation(),
                profile.getDegree(),
                profile.getGraduationYear(),
                profile.getTargetRole(),
                profile.getTargetSkills(),
                profile.getExperienceLevel()
        );

        DashboardResponse dashboardResponse = new DashboardResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                profileResponse
        );

        return ResponseEntity.ok(dashboardResponse);
    }
}