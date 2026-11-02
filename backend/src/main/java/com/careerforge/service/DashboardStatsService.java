package com.careerforge.service;

import com.careerforge.dto.DashboardStatsResponse;
import com.careerforge.repository.JobApplicationRepository;
import com.careerforge.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
public class DashboardStatsService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;

    public DashboardStatsService(
            JobRepository jobRepository,
            JobApplicationRepository jobApplicationRepository) {

        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    public DashboardStatsResponse getStats(Long userId) {

        long totalJobs = jobRepository.count();

        var applications = jobApplicationRepository.findByUserId(userId);

        long totalApplications = applications.size();

        long appliedApplications = applications.stream()
                .filter(application -> "Applied".equalsIgnoreCase(application.getStatus()))
                .count();

        long interviewApplications = applications.stream()
                .filter(application -> "Interview".equalsIgnoreCase(application.getStatus()))
                .count();

        long selectedApplications = applications.stream()
                .filter(application -> "Selected".equalsIgnoreCase(application.getStatus()))
                .count();

        return new DashboardStatsResponse(
                totalJobs,
                totalApplications,
                appliedApplications,
                interviewApplications,
                selectedApplications
        );
    }
}