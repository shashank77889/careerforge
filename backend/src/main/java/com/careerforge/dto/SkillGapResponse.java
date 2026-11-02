package com.careerforge.dto;

import java.util.List;

public class SkillGapResponse {

    private List<String> userSkills;
    private List<String> requiredSkills;
    private List<String> missingSkills;

    public SkillGapResponse() {
    }

    public SkillGapResponse(
            List<String> userSkills,
            List<String> requiredSkills,
            List<String> missingSkills) {

        this.userSkills = userSkills;
        this.requiredSkills = requiredSkills;
        this.missingSkills = missingSkills;
    }

    public List<String> getUserSkills() {
        return userSkills;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }
}