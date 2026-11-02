package com.careerforge.interview;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "interview_sessions", indexes = @Index(columnList = "userId,startedAt"))
public class InterviewSession {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long userId;
    @Column(nullable = false) public String role;
    @Column(nullable = false) public String status = "IN_PROGRESS";
    public int currentQuestion;
    @Lob @Column(columnDefinition = "LONGTEXT", nullable = false) public String questionsJson;
    @Lob @Column(columnDefinition = "LONGTEXT", nullable = false) public String answersJson;
    public Instant startedAt = Instant.now();
    public Instant completedAt;
    @Version public Long version;
}
