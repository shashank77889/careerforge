package com.careerforge.dto;

public class DashboardResponse {

    private Long userId;
    private String name;
    private String email;
    private ProfileResponse profile;

    public DashboardResponse(
            Long userId,
            String name,
            String email,
            ProfileResponse profile) {

        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profile = profile;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public ProfileResponse getProfile() {
        return profile;
    }
}