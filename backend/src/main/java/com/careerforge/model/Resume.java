package com.careerforge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String extractedText;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    public Resume() {
    }

    // ================= GETTERS =================

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public User getUser() {
        return user;
    }

    // ================= SETTERS =================

    public void setId(Long id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public void setUser(User user) {
        this.user = user;
    }
}