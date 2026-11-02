package com.careerforge.service;

import com.careerforge.model.ApplicationStatus;
import com.careerforge.model.Job;
import com.careerforge.model.JobApplication;
import com.careerforge.model.User;
import com.careerforge.repository.JobApplicationRepository;
import com.careerforge.repository.JobRepository;
import com.careerforge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public JobApplicationService(
            JobApplicationRepository jobApplicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository) {

        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    @org.springframework.transaction.annotation.Transactional
    public JobApplication createApplication(Long userId, Job job, String status) {
        validateStatus(status);
        if (job.getTitle() == null || job.getTitle().isBlank() || job.getCompany() == null || job.getCompany().isBlank()) {
            throw new IllegalArgumentException("Job title and company are required");
        }
        job.setTitle(job.getTitle().trim());
        job.setCompany(job.getCompany().trim());
        Job savedJob = jobRepository.save(job);
        return applyForJob(userId, savedJob.getId(), status);
    }

    public JobApplication applyForJob(
            Long userId,
            Long jobId,
            String status) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (jobApplicationRepository.existsByUserIdAndJobId(userId, jobId)) {
            throw new RuntimeException(
                    "You have already applied for this job"
            );
        }

        validateStatus(status);

        JobApplication application = new JobApplication();

        application.setUser(user);
        application.setJob(job);
        application.setStatus(status);

        return jobApplicationRepository.save(application);
    }

    public List<JobApplication> getApplications(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return jobApplicationRepository.findByUserId(userId);
    }

    public JobApplication updateStatus(
            Long applicationId,
            String status) {

        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        Ownership.require(application.getUser().getId());
        validateStatus(status);

        application.setStatus(status);

        return jobApplicationRepository.save(application);
    }

    private void validateStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new RuntimeException(
                    "Application status cannot be empty"
            );
        }

        try {
            ApplicationStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException(
                    "Invalid application status. Allowed values: Applied, Interview, Selected, Rejected"
            );
        }
    }
}

