package com.careerforge.dto;

public class ResumeResponse {

    private Long id;
    private String fileName;
    private String fileUrl;

    public ResumeResponse() {
    }

    public ResumeResponse(Long id, String fileName, String fileUrl) {
        this.id = id;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}