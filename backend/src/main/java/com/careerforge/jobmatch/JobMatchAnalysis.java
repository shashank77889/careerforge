package com.careerforge.jobmatch;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "job_match_analyses", indexes = @Index(name = "idx_job_match_user_updated", columnList = "user_id,updated_at"))
public class JobMatchAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 160)
    private String jobTitle;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String jobDescription;

    @Column(nullable = false, length = 255)
    private String resumeLabel;

    @Column(nullable = false)
    private int score;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String resultJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    void touch() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    public String getResumeLabel() { return resumeLabel; }
    public void setResumeLabel(String resumeLabel) { this.resumeLabel = resumeLabel; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
