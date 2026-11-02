package com.careerforge.service;

import com.careerforge.model.Resume;
import com.careerforge.model.Skill;
import com.careerforge.model.User;
import com.careerforge.repository.ResumeRepository;
import com.careerforge.repository.SkillRepository;
import com.careerforge.repository.UserRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;

    public ResumeService(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            SkillRepository skillRepository) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
    }

    // ================= UPLOAD RESUME =================

    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Resume uploadResume(
            Long userId,
            MultipartFile file) throws IOException {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        String extractedText = extractText(file);
        String fileName = file.getOriginalFilename().trim();

        // =================================================
        // IMPORTANT:
        // We ALWAYS create a NEW Resume object.
        //
        // Therefore:
        //
        // Resume 1 -> database row 1
        // Resume 2 -> database row 2
        // Resume 3 -> database row 3
        //
        // Nothing is overwritten.
        // =================================================

        Resume resume = new Resume();

        resume.setFileName(fileName);
        resume.setExtractedText(extractedText);
        resume.setUser(user);

        Resume savedResume =
                resumeRepository.save(resume);

        // ================= EXTRACT SKILLS =================

        extractAndSaveSkills(
                extractedText,
                user
        );

        return savedResume;
    }

    // ================= GET ALL USER RESUMES =================

    public List<Resume> getUserResumes(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return resumeRepository.findAllByUserId(userId);
    }

    // ================= GET SINGLE RESUME =================

    public Resume getResume(
            Long resumeId,
            Long userId) {

        Resume resume =
                resumeRepository.findById(resumeId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Resume not found"
                                ));

        if (resume.getUser() == null ||
                resume.getUser().getId() == null ||
                !resume.getUser()
                        .getId()
                        .equals(userId)) {

            throw new RuntimeException(
                    "You are not allowed to access this resume"
            );
        }

        return resume;
    }

    // ================= DELETE RESUME =================

    public void deleteResume(
            Long resumeId,
            Long userId) {

        Resume resume =
                getResume(
                        resumeId,
                        userId
                );

        resumeRepository.delete(resume);
    }

    // ================= PDF EXTRACTION =================

    public String extractText(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Please upload a non-empty resume");
        if (file.getSize() > 10 * 1024 * 1024) throw new IllegalArgumentException("Resume must be under 10 MB");
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) throw new IllegalArgumentException("Invalid file name");
        String lowerName = fileName.toLowerCase(java.util.Locale.ROOT);
        String extracted = lowerName.endsWith(".pdf") ? extractPdfText(file)
                : lowerName.endsWith(".docx") ? extractDocxText(file)
                : null;
        if (extracted == null) throw new IllegalArgumentException("Only PDF and DOCX resumes are supported");
        extracted = normalizeExtractedText(extracted);
        if (extracted.isBlank()) throw new IllegalArgumentException("Could not extract readable text from the resume");
        return extracted;
    }

    public static String normalizeExtractedText(String text) {
        if (text == null) return "";
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replace("\u00ad", "")
                .replace('\u00a0', ' ')
                .replaceAll("(?<=\\p{L})-\\R(?=\\p{Ll})", "")
                .replaceAll("[\\t\\x0B\\f]+", " ")
                .replaceAll(" *\\R *", "\n")
                .replaceAll(" {2,}", " ")
                .trim();
    }

    private String extractPdfText(
            MultipartFile file)
            throws IOException {

        try (PDDocument document =
                     Loader.loadPDF(
                             file.getBytes()
                     )) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            stripper.setSortByPosition(true);

            return stripper.getText(document);
        }
    }

    // ================= DOCX EXTRACTION =================

    private String extractDocxText(
            MultipartFile file)
            throws IOException {

        StringBuilder text =
                new StringBuilder();

        try (XWPFDocument document =
                     new XWPFDocument(
                             file.getInputStream()
                     )) {

            document.getTables().forEach(table -> table.getRows().forEach(row -> row.getTableCells().forEach(cell -> text.append(cell.getText()).append("\n"))));
            document.getParagraphs()
                    .forEach(paragraph ->
                            text.append(
                                    paragraph.getText()
                            ).append("\n")
                    );
        }

        return text.toString();
    }

    // ================= SKILL EXTRACTION =================

    private void extractAndSaveSkills(
            String resumeText,
            User user) {

        String text =
                resumeText.toLowerCase();

        Map<String, Integer> detectedSkills =
                new LinkedHashMap<>();

        // ================= PROGRAMMING =================

        detect(text, detectedSkills, "Java");
        detect(text, detectedSkills, "Python");
        detect(text, detectedSkills, "JavaScript");
        detect(text, detectedSkills, "C++");
        detect(text, detectedSkills, "C");

        // ================= WEB =================

        detect(text, detectedSkills, "HTML");
        detect(text, detectedSkills, "CSS");
        detect(text, detectedSkills, "React");
        detect(text, detectedSkills, "Node.js");

        // ================= BACKEND =================

        detect(text, detectedSkills, "Spring Boot");
        detect(text, detectedSkills, "REST API");
        detect(text, detectedSkills, "Express.js");

        // ================= DATABASE =================

        detect(text, detectedSkills, "SQL");
        detect(text, detectedSkills, "MySQL");
        detect(text, detectedSkills, "PostgreSQL");
        detect(text, detectedSkills, "MongoDB");

        // ================= TESTING =================

        detect(text, detectedSkills, "Selenium");
        detect(text, detectedSkills, "Jest");
        detect(text, detectedSkills, "API Testing");

        // ================= AI / ML =================

        detect(text, detectedSkills, "Machine Learning");
        detect(text, detectedSkills, "Deep Learning");
        detect(text, detectedSkills, "NumPy");
        detect(text, detectedSkills, "Pandas");
        detect(text, detectedSkills, "Scikit-learn");
        detect(text, detectedSkills, "TensorFlow");
        detect(text, detectedSkills, "PyTorch");
        detect(text, detectedSkills, "Statistics");

        // ================= DATA SCIENCE =================

        detect(text, detectedSkills, "Data Visualization");
        detect(text, detectedSkills, "Excel");

        // ================= CYBERSECURITY =================

        detect(text, detectedSkills, "Cybersecurity Fundamentals");
        detect(text, detectedSkills, "Django");
        detect(text, detectedSkills, "Flask");
        detect(text, detectedSkills, "Networking");
        detect(text, detectedSkills, "Linux");
        detect(text, detectedSkills, "SIEM");
        detect(text, detectedSkills, "Ethical Hacking");
        detect(text, detectedSkills, "Cryptography");

        // ================= TOOLS =================

        detect(text, detectedSkills, "Git");
        detect(text, detectedSkills, "GitHub");
        detect(text, detectedSkills, "Docker");
        detect(text, detectedSkills, "AWS");

        // ================= SAVE SKILLS =================

        List<Skill> existingSkills =
                skillRepository.findAllByUserId(
                        user.getId()
                );

        for (String skillName :
                detectedSkills.keySet()) {

            boolean alreadyExists =
                    existingSkills
                            .stream()
                            .anyMatch(skill ->
                                    skill.getName()
                                            .equalsIgnoreCase(
                                                    skillName
                                            )
                            );

            // Don't create duplicate skills
            if (alreadyExists) {
                continue;
            }

            Skill skill = new Skill();

            skill.setName(skillName);

            // Resume confirms presence of skill,
            // but does not determine proficiency.
            skill.setProficiency(0);

            // Confidence is unknown.
            skill.setConfidence(0);

            // Default target.
            skill.setTargetLevel(80);

            // Important for distinguishing
            // resume-extracted skills.
            skill.setSource("RESUME");

            skill.setUser(user);

            skillRepository.save(skill);
        }
    }

    // ================= DETECT SKILL =================

    private void detect(
            String text,
            Map<String, Integer> detectedSkills,
            String skill) {

        if (com.careerforge.journey.LearningCatalog.mentions(text, skill)) {

            detectedSkills.put(
                    skill,
                    1
            );
        }
    }
}
