package com.careerforge.controller;

import com.careerforge.dto.ProfileResponse;
import com.careerforge.model.Profile;
import com.careerforge.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> createProfile(
            @PathVariable Long userId,
            @RequestBody Profile profile) {

        Profile createdProfile = profileService.createProfile(userId, profile);

        return new ResponseEntity<>(
                toResponse(createdProfile),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable Long userId) {

        Profile profile = profileService.getProfile(userId);

        return ResponseEntity.ok(toResponse(profile));
    }

    private ProfileResponse toResponse(Profile profile) {

        return new ProfileResponse(
                profile.getId(),
                profile.getEducation(),
                profile.getDegree(),
                profile.getGraduationYear(),
                profile.getTargetRole(),
                profile.getTargetSkills(),
                profile.getExperienceLevel()
        );
    }
}