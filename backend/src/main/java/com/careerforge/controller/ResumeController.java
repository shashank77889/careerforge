package com.careerforge.controller;

import com.careerforge.model.Resume;
import com.careerforge.service.ResumeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")

public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // ================= UPLOAD RESUME =================

    @PostMapping("/user/{userId}")
    public ResponseEntity<?> uploadResume(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {

        try {

            Resume resume = resumeService.uploadResume(
                    userId,
                    file
            );

            return ResponseEntity.ok(resume);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // ================= GET ALL USER RESUMES =================

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserResumes(
            @PathVariable Long userId) {

        try {

            List<Resume> resumes =
                    resumeService.getUserResumes(userId);

            return ResponseEntity.ok(resumes);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // ================= GET SINGLE RESUME =================

    @GetMapping("/{resumeId}/user/{userId}")
    public ResponseEntity<?> getResume(
            @PathVariable Long resumeId,
            @PathVariable Long userId) {

        try {

            Resume resume =
                    resumeService.getResume(
                            resumeId,
                            userId
                    );

            return ResponseEntity.ok(resume);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    // ================= DELETE RESUME =================

    @DeleteMapping("/{resumeId}/user/{userId}")
    public ResponseEntity<?> deleteResume(
            @PathVariable Long resumeId,
            @PathVariable Long userId) {

        try {

            resumeService.deleteResume(
                    resumeId,
                    userId
            );

            return ResponseEntity.noContent().build();

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}
