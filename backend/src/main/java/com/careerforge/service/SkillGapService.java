package com.careerforge.service;

import com.careerforge.dto.SkillGapResponse;
import com.careerforge.model.Skill;
import com.careerforge.model.User;
import com.careerforge.repository.SkillRepository;
import com.careerforge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SkillGapService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SkillService skillService;

    public SkillGapService(
            UserRepository userRepository,
            SkillRepository skillRepository,
            SkillService skillService) {

        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.skillService = skillService;
    }

    // ================= ANALYZE SKILL GAP =================

    public SkillGapResponse analyzeSkillGap(
            Long userId,
            String role) {

        // ================= FIND USER =================

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // ================= VALIDATE ROLE =================

        if (role == null || role.trim().isEmpty()) {
            throw new RuntimeException(
                    "Target career role is required"
            );
        }

        // ================= GET USER SKILLS =================

        List<Skill> skills =
                skillRepository.findAllByUserId(user.getId());

        List<String> userSkills =
                new ArrayList<>();

        for (Skill skill : skills) {

            if (skill.getName() != null &&
                    !skill.getName().trim().isEmpty()) {

                userSkills.add(
                        skill.getName().trim()
                );
            }
        }

        // ================= GET REQUIRED SKILLS =================

        Map<String, Integer> requiredSkillMap =
                skillService.getRequiredSkills(
                        role.trim()
                );

        List<String> requiredSkills =
                new ArrayList<>(
                        requiredSkillMap.keySet()
                );

        // ================= FIND MISSING SKILLS =================

        List<String> missingSkills =
                new ArrayList<>();

        for (String requiredSkill : requiredSkills) {

            boolean hasSkill = false;

            for (String userSkill : userSkills) {

                if (userSkill.equalsIgnoreCase(
                        requiredSkill)) {

                    hasSkill = true;
                    break;
                }
            }

            if (!hasSkill) {
                missingSkills.add(requiredSkill);
            }
        }

        // ================= RETURN RESULT =================

        return new SkillGapResponse(
                userSkills,
                requiredSkills,
                missingSkills
        );
    }
}