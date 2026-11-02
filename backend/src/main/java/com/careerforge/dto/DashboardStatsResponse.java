package com.careerforge.dto;

public class DashboardStatsResponse {

    private long totalJobs;
    private long totalApplications;
    private long appliedApplications;
    private long interviewApplications;
    private long selectedApplications;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(
            long totalJobs,
            long totalApplications,
            long appliedApplications,
            long interviewApplications,
            long selectedApplications) {

        this.totalJobs = totalJobs;
        this.totalApplications = totalApplications;
        this.appliedApplications = appliedApplications;
        this.interviewApplications = interviewApplications;
        this.selectedApplications = selectedApplications;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public long getAppliedApplications() {
        return appliedApplications;
    }

    public long getInterviewApplications() {
        return interviewApplications;
    }

    public long getSelectedApplications() {
        return selectedApplications;
    }
}