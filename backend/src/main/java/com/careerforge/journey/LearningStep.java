package com.careerforge.journey;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "learning_steps", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "role", "skill"}))
public class LearningStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long userId;
    @Column(nullable = false) public String role;
    @Column(nullable = false) public String skill;
    @Column(nullable = false) public String status = "NOT_STARTED";
    @Column(length = 2000) public String reflection = "";
    public Instant completedAt;
    @Version public Long version;
}
