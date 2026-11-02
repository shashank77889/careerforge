package com.careerforge.service;

import com.careerforge.dto.LearningRecommendationResponse;
import com.careerforge.dto.SkillGapResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LearningRecommendationService {

    private final SkillGapService skillGapService;

    public LearningRecommendationService(SkillGapService skillGapService) {
        this.skillGapService = skillGapService;
    }

    public List<LearningRecommendationResponse> getRecommendations(Long userId, String role) {

        SkillGapResponse skillGap = skillGapService.analyzeSkillGap(userId, role);

        List<LearningRecommendationResponse> recommendations = new ArrayList<>();

        for (String skill : skillGap.getMissingSkills()) {

            String recommendation;
            String resourceUrl;

            switch (skill.toLowerCase()) {

                case "git":
                    recommendation = "Learn Git basics, branching, commits, merging, and GitHub workflows.";
                    resourceUrl = "https://git-scm.com/doc";
                    break;

                case "rest api":
                    recommendation = "Learn REST API concepts, HTTP methods, status codes, request/response handling, and API testing.";
                    resourceUrl = "https://developer.mozilla.org/en-US/docs/Glossary/REST";
                    break;

                case "docker":
                    recommendation = "Learn Docker containers, images, Dockerfiles, and basic container management.";
                    resourceUrl = "https://docs.docker.com/get-started/";
                    break;

                default:
                    recommendation = "Explore learning resources and practical projects for " + skill + ".";
                    resourceUrl = "https://www.google.com/search?q=" + skill.replace(" ", "+") + "+tutorial";
                    break;
            }

            recommendations.add(
                    new LearningRecommendationResponse(
                            skill,
                            recommendation,
                            resourceUrl
                    )
            );
        }

        return recommendations;
    }
}
