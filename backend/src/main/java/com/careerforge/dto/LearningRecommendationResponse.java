package com.careerforge.dto;

public class LearningRecommendationResponse {

    private String skill;
    private String recommendation;
    private String resourceUrl;

    public LearningRecommendationResponse() {
    }

    public LearningRecommendationResponse(
            String skill,
            String recommendation,
            String resourceUrl) {

        this.skill = skill;
        this.recommendation = recommendation;
        this.resourceUrl = resourceUrl;
    }

    public String getSkill() {
        return skill;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }
}