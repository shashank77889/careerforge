package com.careerforge.journey;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "readiness_snapshots", indexes = @Index(columnList = "userId,role"))
public class ReadinessSnapshot {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long userId;
    @Column(nullable = false) public String role;
    public int score;
    public Instant recordedAt = Instant.now();
}
