package com.careerforge.controller;

import com.careerforge.dto.UpdateApplicationStatusRequest;
import com.careerforge.model.Job;
import com.careerforge.model.JobApplication;
import com.careerforge.repository.JobRepository;
import com.careerforge.service.JobApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;
    private final JobRepository jobRepository;

    public JobApplicationController(
            JobApplicationService jobApplicationService,
            JobRepository jobRepository) {

        this.jobApplicationService = jobApplicationService;
        this.jobRepository = jobRepository;
    }

    @PostMapping
    public ResponseEntity<?> createApplication(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        Long authenticatedUserId =
                (Long) authentication.getPrincipal();

        try {
            Map<String, Object> jobData =
                    (Map<String, Object>) request.get("job");

            Job job = new Job();

            job.setTitle((String) jobData.get("title"));
            job.setCompany((String) jobData.get("company"));
            job.setLocation((String) jobData.get("location"));
            job.setEmploymentType(
                    (String) jobData.get("employmentType")
            );
            job.setApplicationUrl(
                    (String) jobData.get("applicationUrl")
            );

            String status = request.get("status") == null ? "Applied" : request.get("status").toString();
            JobApplication application = jobApplicationService.createApplication(authenticatedUserId, job, status);
            return ResponseEntity.ok(application);

        } catch (Exception exception) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error",
                            exception.getMessage() != null
                                    ? exception.getMessage()
                                    : "Unable to create application"
                    ));
        }
    }

    @PostMapping("/user/{userId}/job/{jobId}")
    public ResponseEntity<?> applyForJob(
            @PathVariable Long userId,
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "Applied") String status,
            Authentication authentication) {

        Long authenticatedUserId =
                (Long) authentication.getPrincipal();

        if (!authenticatedUserId.equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to access another user's applications");
        }

        return ResponseEntity.ok(
                jobApplicationService.applyForJob(
                        userId,
                        jobId,
                        status
                )
        );
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getApplications(
            @PathVariable Long userId,
            Authentication authentication) {

        Long authenticatedUserId =
                (Long) authentication.getPrincipal();

        if (!authenticatedUserId.equals(userId)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You are not allowed to access another user's applications");
        }

        List<JobApplication> applications =
                jobApplicationService.getApplications(userId);

        return ResponseEntity.ok(applications);
    }

    @PutMapping("/{applicationId}")
    public ResponseEntity<JobApplication> updateStatus(
            @PathVariable Long applicationId,
            @RequestBody UpdateApplicationStatusRequest request) {

        return ResponseEntity.ok(
                jobApplicationService.updateStatus(
                        applicationId,
                        request.getStatus()
                )
        );
    }
}
