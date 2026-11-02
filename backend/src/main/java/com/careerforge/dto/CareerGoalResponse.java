package com.careerforge.dto;

public class CareerGoalResponse {

    private Long id;
    private String targetRole;
    private String skills;
    private String preferredLocation;
    private String employmentType;
    private boolean completed;

    public CareerGoalResponse(
            Long id,
            String targetRole,
            String skills,
            String preferredLocation,
            String employmentType,
            boolean completed) {

        this.id = id;
        this.targetRole = targetRole;
        this.skills = skills;
        this.preferredLocation = preferredLocation;
        this.employmentType = employmentType;
        this.completed = completed;
    }

    public Long getId() {
        return id;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public String getSkills() {
        return skills;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public boolean isCompleted() {
        return completed;
    }
}