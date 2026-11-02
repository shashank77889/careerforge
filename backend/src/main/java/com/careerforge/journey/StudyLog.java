package com.careerforge.journey;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "study_logs", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "role", "studyDate", "sessionKey"}), indexes = @Index(columnList = "userId,role,studyDate"))
public class StudyLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(nullable = false) public Long userId;
    @Column(nullable = false) public String role;
    @Column(nullable = false, length = 180) public String sessionKey;
    @Column(nullable = false) public LocalDate studyDate;
    @Column(nullable = false) public String skill;
    @Column(nullable = false) public int plannedMinutes;
    @Column(nullable = false) public int studiedMinutes;
    @Column(nullable = false) public boolean completed;
    @Column(nullable = false) public Instant updatedAt = Instant.now();
}
