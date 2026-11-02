package com.careerforge.journey;

import com.careerforge.model.Skill;
import com.careerforge.repository.*;
import com.careerforge.service.SkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.util.*;

@Service
public class JourneyService {
    public static final List<String> ROLES = List.of("Data Scientist", "Data Analyst", "AI/ML Engineer", "SDET", "Java Developer", "Python Developer", "Frontend Developer", "Backend Developer", "Cybersecurity Analyst");
    public static final List<String> STUDY_DAYS = List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");
    private final JourneyRepository journeys;
    private final LearningStepRepository steps;
    private final SnapshotRepository snapshots;
    private final SkillRepository skills;
    private final ResumeRepository resumes;
    private final UserRepository users;
    private final SkillService skillService;
    private final LearningCatalog catalog;
    public JourneyService(JourneyRepository journeys, LearningStepRepository steps, SnapshotRepository snapshots, SkillRepository skills, ResumeRepository resumes, UserRepository users, SkillService skillService, LearningCatalog catalog) {
        this.journeys = journeys; this.steps = steps; this.snapshots = snapshots; this.skills = skills;
        this.resumes = resumes; this.users = users; this.skillService = skillService; this.catalog = catalog;
    }
    public record SkillLevel(String skill, int current, int required, String status, String source) {}
    public record Readiness(int score, int coverage, int matched, int missing, int developing, List<SkillLevel> skills, String basis) {}
    public record StepView(Long id, String skill, String status, String reflection, Instant completedAt, LearningCatalog.Resource resource, int week, String reason) {}
    public record StudyPreferences(List<String> days, String startTime, int sessionMinutes, List<String> focusSkills) {}
    public record Overview(String lastPage, String role, int weeklyHours, StudyPreferences studyPreferences, boolean configured, Readiness readiness, List<StepView> plan, int completed, int total, int estimatedWeeks, List<ReadinessSnapshot> history, List<String> roles) {}
    public record PortfolioProject(String title, String role, List<String> skills, String problem,
                                   List<String> deliverables, List<String> proofChecklist, String resumeBulletTemplate) {}

    public CareerJourney preferences(Long userId) {
        return journeys.findById(userId).orElseGet(() -> { var journey = new CareerJourney(); journey.userId = userId; return journey; });
    }
    public Readiness readiness(Long userId, String role) {
        if (!ROLES.contains(role)) throw new IllegalArgumentException("Choose a supported target role");
        var required = skillService.getRequiredSkills(role);
        var current = new HashMap<String, Skill>();
        for (var skill : skills.findAllByUserId(userId)) {
            String key = skill.getName().trim().toLowerCase(Locale.ROOT);
            if (!current.containsKey(key) || proficiency(skill) > proficiency(current.get(key))) current.put(key, skill);
        }
        var levels = new ArrayList<SkillLevel>();
        int achieved = 0, target = 0, present = 0, matched = 0, missing = 0;
        for (var entry : required.entrySet()) {
            var skill = current.get(entry.getKey().toLowerCase(Locale.ROOT));
            int value = skill == null ? 0 : proficiency(skill);
            String status = skill == null ? "MISSING" : value >= entry.getValue() ? "MATCHED" : "DEVELOPING";
            if (skill != null) present++;
            if (status.equals("MATCHED")) matched++;
            if (status.equals("MISSING")) missing++;
            achieved += Math.min(value, entry.getValue()); target += entry.getValue();
            levels.add(new SkillLevel(entry.getKey(), value, entry.getValue(), status, skill == null ? "NOT_ADDED" : skill.getSource()));
        }
        return new Readiness((int)Math.round(100.0 * achieved / target), (int)Math.round(100.0 * present / required.size()), matched, missing, required.size() - matched - missing, levels,
            "Self-reported skill readiness. Sum of capped proficiency divided by sum of role targets. Role targets are CareerForge learning benchmarks, not employer requirements or a prediction of hiring success.");
    }
    private int proficiency(Skill skill) { return Math.max(0, Math.min(100, skill.getProficiency() == null ? 0 : skill.getProficiency())); }
    public Overview overview(Long userId) {
        var pref = preferences(userId);
        var readiness = readiness(userId, pref.targetRole);
        var records = steps.findAllByUserIdAndRoleOrderByIdAsc(userId, pref.targetRole);
        records.sort(Comparator.comparingInt(step -> catalog.get(step.skill).order()));
        var plan = new ArrayList<StepView>();
        int hours = 0, remaining = 0, complete = 0;
        for (var step : records) {
            var resource = catalog.get(step.skill);
            int week = 1 + hours / pref.weeklyHours;
            hours += resource.hours();
            if (step.status.equals("COMPLETED")) complete++; else remaining += resource.hours();
            var level = readiness.skills().stream().filter(item -> item.skill().equals(step.skill)).findFirst().orElseThrow();
            String reason = level.status().equals("MISSING") ? "Missing from your current toolkit" : level.status().equals("MATCHED") ? "Your current self-rating meets this target; keep this milestone as practice" : "Develop from " + level.current() + "% toward the " + level.required() + "% role target";
            plan.add(new StepView(step.id, step.skill, step.status, step.reflection, step.completedAt, resource, week, reason));
        }
        var validSkills = readiness.skills().stream().map(SkillLevel::skill).collect(java.util.stream.Collectors.toSet());
        var focusSkills = csv(pref.focusSkills).stream().filter(validSkills::contains).toList();
        var studyPreferences = new StudyPreferences(csv(pref.studyDays), pref.studyTime, pref.sessionMinutes, focusSkills);
        return new Overview(pref.lastPage, pref.targetRole, pref.weeklyHours, studyPreferences, pref.version != null, readiness, plan, complete, plan.size(), (int)Math.ceil((double)remaining / pref.weeklyHours), snapshots.findTop12ByUserIdAndRoleOrderByRecordedAtDesc(userId, pref.targetRole), ROLES);
    }
    @Transactional
    public void rememberPage(Long userId, String path) {
        if (!List.of("/dashboard", "/goals", "/applications", "/skills", "/learning", "/interviews", "/job-match").contains(path)) throw new IllegalArgumentException("Unknown workspace page");
        users.lockById(userId).orElseThrow();
        var pref = preferences(userId); pref.lastPage = path; journeys.save(pref);
    }
    @Transactional
    public Overview configure(Long userId, String role, int hours) {
        users.lockById(userId).orElseThrow();
        if (!ROLES.contains(role) || hours < 1 || hours > 40) throw new IllegalArgumentException("Choose a supported role and 1–40 study hours per week");
        var pref = preferences(userId);
        if (!pref.targetRole.equals(role)) pref.focusSkills = "";
        pref.targetRole = role; pref.weeklyHours = hours;
        journeys.saveAndFlush(pref);
        return overview(userId);
    }
    @Transactional
    public Overview configureSchedule(Long userId, int hours, List<String> days, String startTime, int sessionMinutes, List<String> focusSkills) {
        users.lockById(userId).orElseThrow();
        if (hours < 1 || hours > 40) throw new IllegalArgumentException("Choose 1–40 study hours per week");
        if (days == null || days.isEmpty() || days.size() > 7 || new HashSet<>(days).size() != days.size() || !STUDY_DAYS.containsAll(days)) throw new IllegalArgumentException("Choose between one and seven valid study days");
        if (startTime == null || !startTime.matches("(?:[01]\\d|2[0-3]):[0-5]\\d")) throw new IllegalArgumentException("Choose a valid study start time");
        if (!List.of(30, 45, 60, 90, 120).contains(sessionMinutes)) throw new IllegalArgumentException("Choose a 30, 45, 60, 90, or 120 minute session");
        var allowedSkills = readiness(userId, preferences(userId).targetRole).skills().stream().filter(skill -> !skill.status().equals("MATCHED")).map(SkillLevel::skill).collect(java.util.stream.Collectors.toSet());
        var selected = focusSkills == null ? List.<String>of() : focusSkills.stream().map(String::trim).filter(value -> !value.isBlank()).distinct().toList();
        if (selected.size() > 8 || !allowedSkills.containsAll(selected)) throw new IllegalArgumentException("Choose focus skills from your current role gaps");
        var pref = preferences(userId);
        pref.weeklyHours = hours; pref.studyDays = String.join(",", days); pref.studyTime = startTime; pref.sessionMinutes = sessionMinutes; pref.focusSkills = String.join(",", selected);
        journeys.saveAndFlush(pref);
        return overview(userId);
    }

    private List<String> csv(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(",")).map(String::trim).filter(item -> !item.isBlank()).toList();
    }
    @Transactional
    public Overview generate(Long userId) {
        users.lockById(userId).orElseThrow();
        var pref = preferences(userId);
        if (pref.version == null) journeys.saveAndFlush(pref);
        var existing = steps.findAllByUserIdAndRoleOrderByIdAsc(userId, pref.targetRole);
        for (var level : readiness(userId, pref.targetRole).skills()) {
            if (level.status().equals("MATCHED") || existing.stream().anyMatch(step -> step.skill.equals(level.skill()))) continue;
            catalog.get(level.skill());
            var step = new LearningStep(); step.userId = userId; step.role = pref.targetRole; step.skill = level.skill(); steps.save(step);
        }
        steps.flush();
        return overview(userId);
    }
    @Transactional
    public Overview updateStep(Long userId, Long stepId, String status, String reflection) {
        var step = steps.findById(stepId).filter(value -> value.userId.equals(userId)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Learning milestone not found"));
        if (!List.of("NOT_STARTED", "IN_PROGRESS", "COMPLETED").contains(status)) throw new IllegalArgumentException("Choose a valid milestone status");
        if (reflection == null || reflection.length() > 2000) throw new IllegalArgumentException("Keep practice notes under 2,000 characters");
        if (status.equals("COMPLETED") && reflection.trim().length() < 40) throw new IllegalArgumentException("Before completing, describe your practical task, how you checked it, and what still needs practice (at least 40 characters).");
        step.status = status; step.reflection = reflection.trim();
        step.completedAt = status.equals("COMPLETED") ? step.completedAt == null ? Instant.now() : step.completedAt : null;
        steps.saveAndFlush(step);
        return overview(userId);
    }
    @Transactional
    public Overview reassess(Long userId) {
        users.lockById(userId).orElseThrow();
        var pref = preferences(userId); var snapshot = new ReadinessSnapshot();
        snapshot.userId = userId; snapshot.role = pref.targetRole; snapshot.score = readiness(userId, pref.targetRole).score();
        snapshots.saveAndFlush(snapshot);
        return overview(userId);
    }
    public List<Map<String, Object>> resumeEvidence(Long userId) {
        var documents = resumes.findAllByUserId(userId);
        documents.sort(Comparator.comparing(com.careerforge.model.Resume::getId).reversed());
        return documents.stream().limit(5).map(resume -> {
            String text = com.careerforge.service.ResumeService.normalizeExtractedText(resume.getExtractedText());
            var evidence = catalog.all().stream().filter(resource -> LearningCatalog.mentions(text, resource.skill())).map(resource -> {
                int index = text.toLowerCase(Locale.ROOT).indexOf(resource.skill().toLowerCase(Locale.ROOT));
                String excerpt = evidenceExcerpt(text, index, resource.skill().length());
                return Map.of("skill", resource.skill(), "excerpt", excerpt);
            }).toList();
            return Map.<String, Object>of("id", resume.getId(), "fileName", resume.getFileName(), "evidence", evidence);
        }).toList();
    }
    private String evidenceExcerpt(String text, int matchIndex, int matchLength) {
        if (text.isBlank() || matchIndex < 0) return "Mention detected; open the resume to verify the surrounding statement.";
        int start = Math.max(0, matchIndex - 130);
        int end = Math.min(text.length(), matchIndex + matchLength + 190);
        if (start > 0) {
            int sentence = previousSentenceBoundary(text, matchIndex - 1, start);
            start = sentence >= start ? sentence + 1 : advanceToBoundary(text, start);
        }
        if (end < text.length()) {
            int sentence = nextSentenceBoundary(text, matchIndex + matchLength, end);
            end = sentence >= 0 ? sentence + 1 : retreatToBoundary(text, end);
        }
        String excerpt = text.substring(start, Math.max(start, end)).replaceAll("\\s+", " ").trim();
        return (start > 0 ? "…" : "") + excerpt + (end < text.length() ? "…" : "");
    }
    private int advanceToBoundary(String text, int index) {
        while (index < text.length() && !Character.isWhitespace(text.charAt(index))) index++;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) index++;
        return index;
    }
    private int retreatToBoundary(String text, int index) {
        while (index > 0 && !Character.isWhitespace(text.charAt(index - 1))) index--;
        return index;
    }
    private int previousSentenceBoundary(String text, int from, int limit) {
        for (int index = Math.min(from, text.length() - 1); index >= Math.max(0, limit); index--) {
            if (isSentenceBoundary(text, index)) return index;
        }
        return -1;
    }
    private int nextSentenceBoundary(String text, int from, int limit) {
        for (int index = from; index < Math.min(limit, text.length()); index++) {
            if (isSentenceBoundary(text, index)) return index;
        }
        return -1;
    }
    private boolean isSentenceBoundary(String text, int index) {
        char value = text.charAt(index);
        if (value == '\n') return true;
        if (value != '.' && value != '!' && value != '?') return false;
        return index + 1 >= text.length() || Character.isWhitespace(text.charAt(index + 1));
    }
    public Map<String, Object> compareJob(Long userId, String description) {
        if (description == null || description.isBlank() || description.length() > 15000) throw new IllegalArgumentException("Paste a job description of up to 15,000 characters");
        var detected = catalog.all().stream().filter(resource -> LearningCatalog.mentions(description, resource.skill())).map(LearningCatalog.Resource::skill).toList();
        var mine = skills.findAllByUserId(userId);
        var matched = detected.stream().filter(skill -> mine.stream().anyMatch(s -> s.getName().equalsIgnoreCase(skill))).toList();
        var missing = detected.stream().filter(skill -> !matched.contains(skill)).toList();
        return Map.of("detected", detected, "matched", matched, "missing", missing, "coverage", detected.isEmpty() ? 0 : Math.round(100.0 * matched.size() / detected.size()), "method", "Local keyword comparison. Mentions may describe optional skills or negations. Review the job description; this is not an ATS score or hiring prediction.");
    }
    public PortfolioProject portfolioProject(Long userId) {
        var pref = preferences(userId);
        var selected = readiness(userId, pref.targetRole).skills().stream()
            .filter(skill -> !skill.status().equals("MATCHED")).map(SkillLevel::skill).limit(3).toList();
        if (selected.isEmpty()) selected = readiness(userId, pref.targetRole).skills().stream().map(SkillLevel::skill).limit(3).toList();
        String title;
        String problem;
        List<String> deliverables;
        switch (pref.targetRole) {
            case "Data Scientist", "Data Analyst" -> {
                title = "Opportunity Pulse";
                problem = "Turn a public employment dataset into a clear, reproducible analysis that helps students understand which skills appear in real opportunities.";
                deliverables = List.of("A documented data-cleaning pipeline", "An exploratory dashboard with honest limitations", "A short findings report and reproducible repository");
            }
            case "AI/ML Engineer" -> {
                title = "Career Skill Classifier";
                problem = "Build and evaluate a small model that maps anonymized job descriptions to career skill categories, including a transparent baseline and error analysis.";
                deliverables = List.of("A reproducible training and evaluation pipeline", "Baseline comparison and error analysis", "A small inference API with monitoring notes");
            }
            case "SDET" -> {
                title = "CareerForge Quality Lab";
                problem = "Create a reliable automated test strategy for the most important CareerForge user journeys.";
                deliverables = List.of("Risk-based test plan and traceable cases", "API and browser automation for critical workflows", "CI report with documented flaky-test prevention");
            }
            case "Java Developer", "Backend Developer" -> {
                title = "Mentorship Match API";
                problem = "Design a secure service that matches learners with mentors using skills, availability, and goals.";
                deliverables = List.of("Validated REST API and relational data model", "Authentication, ownership checks, and useful errors", "Integration tests plus API documentation");
            }
            case "Python Developer" -> {
                title = "Learning Progress Service";
                problem = "Build a Python service that turns a learner's goals into tracked weekly activities and progress summaries.";
                deliverables = List.of("Validated API and database persistence", "Background-safe import or reporting workflow", "Automated tests and deployment instructions");
            }
            case "Frontend Developer" -> {
                title = "Accessible Opportunity Explorer";
                problem = "Help job seekers compare opportunities through a fast, responsive, keyboard-friendly interface.";
                deliverables = List.of("Responsive search and filtering with URL state", "Accessible loading, empty, error, and form states", "Component tests and a documented usability review");
            }
            case "Cybersecurity Analyst" -> {
                title = "Authentication Signal Lab";
                problem = "Analyze synthetic login events in an isolated lab and explain how suspicious activity should be triaged.";
                deliverables = List.of("Synthetic event generator and detection rules", "Triage dashboard with false-positive analysis", "Incident report template and remediation guidance");
            }
            default -> throw new IllegalStateException("No portfolio project for role");
        }
        var checklist = List.of("Public repository with a clear problem statement and setup", "Screenshots or a short demo showing the working result", "Tests for one important success path and one failure path", "README section explaining decisions, limitations, and what you learned");
        String skillsText = String.join(", ", selected);
        String bullet = "Built " + title + " using " + skillsText + " to [describe user outcome]; validated it with [test or measurable result].";
        return new PortfolioProject(title, pref.targetRole, selected, problem, deliverables, checklist, bullet);
    }
}

