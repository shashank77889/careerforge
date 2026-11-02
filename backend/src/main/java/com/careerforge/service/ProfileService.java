package com.careerforge.service;

import com.careerforge.model.Profile;
import com.careerforge.model.User;
import com.careerforge.repository.ProfileRepository;
import com.careerforge.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(
            ProfileRepository profileRepository,
            UserRepository userRepository) {

        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    public Profile createProfile(Long userId, Profile profile) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        profile.setUser(user);

        return profileRepository.save(profile);
    }

    public Profile getProfile(Long userId) {

        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
    }
}