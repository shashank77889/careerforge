package com.careerforge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer proficiency = 0;

    @Column(nullable = false)
    private Integer confidence = 0;

    @Column(nullable = false)
    private Integer targetLevel = 80;

    @Column(nullable = false)
    private String source = "MANUAL";

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================= CONSTRUCTOR =================

    public Skill() {
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getProficiency() {
        return proficiency;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public Integer getTargetLevel() {
        return targetLevel;
    }

    public String getSource() {
        return source;
    }

    public User getUser() {
        return user;
    }

    // ================= SETTERS =================

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProficiency(Integer proficiency) {
        this.proficiency = proficiency;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public void setTargetLevel(Integer targetLevel) {
        this.targetLevel = targetLevel;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setUser(User user) {
        this.user = user;
    }
}