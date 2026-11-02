package com.careerforge.service;

import com.careerforge.model.Skill;
import com.careerforge.model.User;
import com.careerforge.repository.SkillRepository;
import com.careerforge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkillService {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    public SkillService(
            SkillRepository skillRepository,
            UserRepository userRepository) {

        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
    }

    // ================= GET USER SKILLS =================

    public List<Skill> getSkills(Long userId) {

        return skillRepository.findAllByUserId(userId);
    }

    // ================= CREATE SKILL =================

    public Skill createSkill(
            Long userId,
            String name,
            Integer proficiency,
            Integer confidence,
            Integer targetLevel) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (name == null || name.isBlank() || name.length() > 100) throw new IllegalArgumentException("Enter a skill name of 1–100 characters");
        validateLevel(proficiency); validateLevel(confidence); validateLevel(targetLevel);
        name = name.trim();
        final String canonicalName = name;
        if (skillRepository.findAllByUserId(userId).stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(canonicalName))) {
            throw new IllegalArgumentException("This skill is already in your toolkit. Update its proficiency instead.");
        }
        Skill skill = new Skill();

        skill.setName(name);
        skill.setProficiency(proficiency);
        skill.setConfidence(confidence);
        skill.setTargetLevel(targetLevel);
        skill.setUser(user);

        return skillRepository.save(skill);
    }

    // ================= UPDATE SKILL =================

    public Skill updateSkill(
            Long skillId,
            Long userId,
            Integer proficiency,
            Integer confidence,
            Integer targetLevel) {

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new RuntimeException("Skill not found"));

        if (skill.getUser() == null ||
                !skill.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You are not allowed to update this skill"
            );
        }

        validateLevel(proficiency); validateLevel(confidence); validateLevel(targetLevel);
        if (proficiency != null) {
            skill.setProficiency(proficiency);
        }

        if (confidence != null) {
            skill.setConfidence(confidence);
        }

        if (targetLevel != null) {
            skill.setTargetLevel(targetLevel);
        }

        return skillRepository.save(skill);
    }

    // ================= DELETE SKILL =================

    public void deleteSkill(
            Long skillId,
            Long userId) {

        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() ->
                        new RuntimeException("Skill not found"));

        if (skill.getUser() == null ||
                !skill.getUser().getId().equals(userId)) {

            throw new RuntimeException(
                    "You are not allowed to delete this skill"
            );
        }

        skillRepository.delete(skill);
    }

    // ================= REQUIRED SKILLS BY ROLE =================

    private void validateLevel(Integer value) {
        if (value != null && (value < 0 || value > 100)) throw new IllegalArgumentException("Skill levels must be between 0 and 100");
    }

    public Map<String, Integer> getRequiredSkills(String role) {

        Map<String, Integer> skills =
                new LinkedHashMap<>();

        switch (role.toLowerCase()) {

            // ================= SDET =================

            case "sdet":

                skills.put("Java", 80);
                skills.put("Selenium", 85);
                skills.put("SQL", 75);
                skills.put("API Testing", 80);
                skills.put("Git", 70);
                skills.put("Jest", 65);

                break;

            // ================= AI / ML =================

            case "ai/ml engineer":
            case "ai engineer":
            case "machine learning engineer":

                skills.put("Python", 90);
                skills.put("Machine Learning", 90);
                skills.put("Statistics", 80);
                skills.put("NumPy", 75);
                skills.put("Pandas", 80);
                skills.put("Scikit-learn", 85);
                skills.put("Deep Learning", 80);
                skills.put("SQL", 70);
                skills.put("Git", 70);

                break;

            // ================= DATA SCIENTIST =================

            case "data scientist":

                skills.put("Python", 90);
                skills.put("Statistics", 90);
                skills.put("Machine Learning", 85);
                skills.put("SQL", 80);
                skills.put("Pandas", 85);
                skills.put("NumPy", 80);
                skills.put("Data Visualization", 75);
                skills.put("Scikit-learn", 80);

                break;

            // ================= DATA ANALYST =================

            case "data analyst":

                skills.put("SQL", 90);
                skills.put("Excel", 85);
                skills.put("Python", 75);
                skills.put("Statistics", 80);
                skills.put("Pandas", 75);
                skills.put("Data Visualization", 80);

                break;

            // ================= CYBERSECURITY =================

            case "cybersecurity analyst":
            case "cybersecurity":

                skills.put("Networking", 85);
                skills.put("Linux", 80);
                skills.put("Cybersecurity Fundamentals", 90);
                skills.put("Python", 70);
                skills.put("SIEM", 75);
                skills.put("Ethical Hacking", 75);
                skills.put("Cryptography", 70);

                break;

            // ================= JAVA DEVELOPER =================

            case "java developer":

                skills.put("Java", 90);
                skills.put("Spring Boot", 85);
                skills.put("SQL", 75);
                skills.put("REST API", 80);
                skills.put("Git", 70);

                break;

            // ================= PYTHON DEVELOPER =================

            case "python developer":

                skills.put("Python", 90);
                skills.put("Django", 75);
                skills.put("Flask", 75);
                skills.put("SQL", 75);
                skills.put("Git", 70);

                break;

            // ================= FRONTEND =================

            case "frontend developer":

                skills.put("HTML", 85);
                skills.put("CSS", 85);
                skills.put("JavaScript", 90);
                skills.put("React", 85);
                skills.put("Git", 70);

                break;

            // ================= BACKEND =================

            case "backend developer":

                skills.put("Java", 80);
                skills.put("Spring Boot", 85);
                skills.put("REST API", 85);
                skills.put("SQL", 80);
                skills.put("Git", 70);

                break;

            // ================= DEFAULT =================

            default:

                skills.put("Python", 70);
                skills.put("SQL", 70);
                skills.put("Git", 70);
                skills.put("Communication", 70);
        }

        return skills;
    }
}
