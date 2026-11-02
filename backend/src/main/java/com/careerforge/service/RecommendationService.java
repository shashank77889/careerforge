package com.careerforge.service;

import com.careerforge.model.CareerGoal;
import com.careerforge.model.Job;
import com.careerforge.repository.CareerGoalRepository;
import com.careerforge.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final JobRepository jobRepository;
    private final CareerGoalRepository careerGoalRepository;

    public RecommendationService(
            JobRepository jobRepository,
            CareerGoalRepository careerGoalRepository) {

        this.jobRepository = jobRepository;
        this.careerGoalRepository = careerGoalRepository;
    }

    public List<Job> getRecommendations(Long userId) {

        List<CareerGoal> goals =
                careerGoalRepository.findAllByUserId(userId);

        if (goals.isEmpty()) {
            throw new RuntimeException("Career goals not found");
        }

        return jobRepository.findAll()
                .stream()
                .filter(job -> {

                    if (job.getTitle() == null ||
                            job.getRequiredSkills() == null) {
                        return false;
                    }

                    String jobTitle =
                            job.getTitle().toLowerCase(Locale.ROOT);

                    String requiredSkills =
                            job.getRequiredSkills().toLowerCase(Locale.ROOT);

                    return goals.stream().anyMatch(goal -> {

                        String targetRole =
                                goal.getTargetRole() == null
                                        ? ""
                                        : goal.getTargetRole()
                                                .toLowerCase(Locale.ROOT);

                        String skills =
                                goal.getSkills() == null
                                        ? ""
                                        : goal.getSkills()
                                                .toLowerCase(Locale.ROOT);

                        boolean roleMatch =
                                !targetRole.isEmpty()
                                && (jobTitle.contains(targetRole)
                                || targetRole.contains(jobTitle));

                        boolean skillMatch =
                                !skills.isEmpty()
                                && Arrays.stream(requiredSkills.split(","))
                                        .map(String::trim)
                                        .anyMatch(skills::contains);

                        return roleMatch || skillMatch;
                    });
                })
                .collect(Collectors.toList());
    }
}