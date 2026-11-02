package com.careerforge.dto;

public class ProfileResponse {

    private Long id;
    private String education;
    private String degree;
    private Integer graduationYear;
    private String targetRole;
    private String targetSkills;
    private String experienceLevel;

    public ProfileResponse(
            Long id,
            String education,
            String degree,
            Integer graduationYear,
            String targetRole,
            String targetSkills,
            String experienceLevel) {

        this.id = id;
        this.education = education;
        this.degree = degree;
        this.graduationYear = graduationYear;
        this.targetRole = targetRole;
        this.targetSkills = targetSkills;
        this.experienceLevel = experienceLevel;
    }

    public Long getId() {
        return id;
    }

    public String getEducation() {
        return education;
    }

    public String getDegree() {
        return degree;
    }

    public Integer getGraduationYear() {
        return graduationYear;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public String getTargetSkills() {
        return targetSkills;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }
}