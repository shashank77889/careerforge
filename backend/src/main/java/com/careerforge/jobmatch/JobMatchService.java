package com.careerforge.jobmatch;

import com.careerforge.journey.LearningCatalog;
import com.careerforge.model.Resume;
import com.careerforge.model.Skill;
import com.careerforge.repository.ResumeRepository;
import com.careerforge.repository.SkillRepository;
import com.careerforge.service.ResumeService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class JobMatchService {
    private static final Set<String> STOP_WORDS = Set.of(
            "about", "after", "also", "and", "are", "been", "being", "candidate", "company", "from", "have", "into",
            "job", "more", "must", "our", "role", "should", "that", "the", "their", "this", "through", "using", "will",
            "with", "work", "working", "years", "your", "you", "responsibilities", "requirements", "preferred", "required"
    );
    private static final List<String> IMPORTANT_PHRASES = List.of(
            "communication", "problem solving", "analytical", "collaboration", "teamwork", "stakeholders", "agile", "scrum",
            "data structures", "algorithms", "object oriented", "unit testing", "integration testing", "ci/cd", "microservices",
            "cloud", "kubernetes", "azure", "gcp", "deployment", "documentation", "leadership", "mentoring"
    );
    private static final Map<String, List<String>> ALIASES = Map.ofEntries(
            Map.entry("JavaScript", List.of("javascript", "java script", "ecmascript", "js")),
            Map.entry("Node.js", List.of("node.js", "nodejs", "node js")),
            Map.entry("Spring Boot", List.of("spring boot", "spring framework")),
            Map.entry("REST API", List.of("rest api", "restful api", "rest services", "web services")),
            Map.entry("Scikit-learn", List.of("scikit-learn", "scikit learn", "sklearn")),
            Map.entry("Machine Learning", List.of("machine learning", "ml models", "predictive modeling")),
            Map.entry("Deep Learning", List.of("deep learning", "neural networks")),
            Map.entry("Data Visualization", List.of("data visualization", "data visualisation", "dashboards")),
            Map.entry("API Testing", List.of("api testing", "postman", "rest assured")),
            Map.entry("Cybersecurity Fundamentals", List.of("cybersecurity", "cyber security", "application security")),
            Map.entry("C++", List.of("c++", "cpp")),
            Map.entry("AWS", List.of("aws", "amazon web services"))
    );

    private final JobMatchRepository analyses;
    private final com.careerforge.repository.UserRepository users;
    private final SkillRepository skills;
    private final ResumeRepository resumes;
    private final ResumeService resumeService;
    private final LearningCatalog catalog;
    private final JsonMapper json;

    public JobMatchService(JobMatchRepository analyses, SkillRepository skills, ResumeRepository resumes,
                           ResumeService resumeService, LearningCatalog catalog, JsonMapper json, com.careerforge.repository.UserRepository users) {
        this.users=users;
        this.analyses = analyses;
        this.skills = skills;
        this.resumes = resumes;
        this.resumeService = resumeService;
        this.catalog = catalog;
        this.json = json;
    }

    public record AnalyzeRequest(String jobTitle, String jobDescription, String resumeText, boolean useSavedResume) {}
    public record SkillAssessment(String skill, String status, int profileLevel, String evidence, String guidance) {}
    public record KeywordAssessment(String keyword, String status) {}
    public record RequirementAssessment(String type, String text, String status, List<String> reflectedTerms) {}
    public record ResumeSuggestion(String title, String guidance, String example, String truthCheck) {}
    public record LearningRecommendation(String skill, String provider, String url, String practice, int hours) {}
    public record Result(int score, String scoreLabel, List<SkillAssessment> skills, List<KeywordAssessment> keywords,
                         List<RequirementAssessment> requirements, List<ResumeSuggestion> suggestions,
                         List<LearningRecommendation> learningRecommendations, String method, String integrityNote, ConceptMatcher.Comparison concepts, List<String> resumePassages) {}
    public record View(Long id, String jobTitle, String jobDescription, String resumeLabel, int score,
                       Instant createdAt, Instant updatedAt, Result result) {}
    public record Summary(Long id, String jobTitle, String resumeLabel, int score, Instant updatedAt) {}

    @Transactional
    public View analyzeText(Long userId, AnalyzeRequest request) {
        validateJob(request == null ? null : request.jobDescription());
        String pasted = request.resumeText() == null ? "" : ResumeService.normalizeExtractedText(request.resumeText());
        if (!pasted.isBlank() && request.useSavedResume()) throw new IllegalArgumentException("Choose pasted text or your latest saved resume, not both");
        if (pasted.length() > 60000) throw new IllegalArgumentException("Keep pasted resume text under 60,000 characters");
        String label = "Pasted resume";
        if (pasted.isBlank()) {
            if (!request.useSavedResume()) throw new IllegalArgumentException("Paste a resume, upload a resume, or use your latest saved resume");
            Resume saved = latestResume(userId);
            pasted = ResumeService.normalizeExtractedText(saved.getExtractedText());
            label = saved.getFileName();
        }
        return save(userId, request.jobTitle(), request.jobDescription(), pasted, label);
    }

    @Transactional
    public View analyzeFile(Long userId, String jobTitle, String jobDescription, MultipartFile file) throws IOException {
        validateJob(jobDescription);
        String text = resumeService.extractText(file);
        return save(userId, jobTitle, jobDescription, text, file.getOriginalFilename().trim());
    }

    public List<Summary> list(Long userId) {
        return analyses.findTop20ByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(value -> new Summary(value.getId(), value.getJobTitle(), value.getResumeLabel(), value.getScore(), value.getUpdatedAt()))
                .toList();
    }

    public View get(Long userId, Long id) {
        return toView(analyses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job match analysis not found")));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        analyses.delete(analyses.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job match analysis not found")));
    }

    private View save(Long userId, String requestedTitle, String description, String resumeText, String resumeLabel) {
        users.lockById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Please sign in again"));
        String cleanJob = ResumeService.normalizeExtractedText(description);
        Result result = evaluate(userId, cleanJob, resumeText);
        JobMatchAnalysis entity = new JobMatchAnalysis();
        entity.setUserId(userId);
        entity.setJobTitle(resolveTitle(requestedTitle, cleanJob));
        entity.setJobDescription(cleanJob);
        entity.setResumeLabel(resumeLabel);
        entity.setScore(result.score());
        try { entity.setResultJson(json.writeValueAsString(result)); }
        catch (JacksonException exception) { throw new IllegalStateException("Unable to save the analysis", exception); }
        return toView(analyses.saveAndFlush(entity));
    }

    private Result evaluate(Long userId, String jobText, String resumeText) {
        String jobLower = jobText.toLowerCase(Locale.ROOT);
        String resumeLower = resumeText.toLowerCase(Locale.ROOT);
        List<Skill> profile = skills.findAllByUserId(userId);
        LinkedHashSet<String> required = new LinkedHashSet<>();
        catalog.all().forEach(resource -> { if (mentionsSkill(jobLower, resource.skill())) required.add(resource.skill()); });
        profile.forEach(skill -> { if (mentionsPhrase(jobLower, skill.getName())) required.add(skill.getName()); });

        List<SkillAssessment> skillResults = required.stream().map(name -> {
            Skill profileSkill = profile.stream().filter(value -> value.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
            boolean inResume = mentionsSkill(resumeLower, name);
            String status = inResume ? "MATCHED" : profileSkill != null ? "PROFILE_ONLY" : "MISSING";
            String guidance = inResume
                    ? "Keep the claim and strengthen it with the task, action, and verifiable result."
                    : profileSkill != null
                    ? "This appears in your CareerForge profile but is not clear in this resume. Mention it only beside work you can explain and demonstrate."
                    : "This is not supported by your resume or CareerForge profile. Learn and practice it before adding it as a skill.";
            return new SkillAssessment(name, status, profileSkill == null ? 0 : profileSkill.getProficiency(),
                    inResume ? excerpt(resumeText, firstSkillIndex(resumeLower, name)) : "", guidance);
        }).toList();

        List<String> keywordCandidates = keywordCandidates(jobLower, required);
        List<KeywordAssessment> keywordResults = keywordCandidates.stream()
                .map(keyword -> new KeywordAssessment(keyword, mentionsPhrase(resumeLower, keyword) ? "MATCHED" : "MISSING"))
                .toList();

        List<RequirementAssessment> qualifications = requirementLines(jobText, "QUALIFICATION").stream()
                .map(text -> assessRequirement("QUALIFICATION", text, resumeLower)).toList();
        List<RequirementAssessment> responsibilities = requirementLines(jobText, "RESPONSIBILITY").stream()
                .map(text -> assessRequirement("RESPONSIBILITY", text, resumeLower)).toList();
        List<RequirementAssessment> requirementResults = new ArrayList<>(qualifications);
        requirementResults.addAll(responsibilities);

        int score = weightedScore(skillResults, keywordResults, qualifications, responsibilities);
        List<ResumeSuggestion> suggestions = suggestions(skillResults, keywordResults, requirementResults);
        Map<String, LearningCatalog.Resource> resources = catalog.all().stream()
                .collect(Collectors.toMap(LearningCatalog.Resource::skill, value -> value, (a, b) -> a, LinkedHashMap::new));
        List<LearningRecommendation> learning = skillResults.stream().filter(value -> value.status().equals("MISSING"))
                .map(value -> resources.get(value.skill())).filter(Objects::nonNull).limit(5)
                .map(value -> new LearningRecommendation(value.skill(), value.provider(), value.url(), value.practice(), value.hours())).toList();

        return new Result(score, score >= 80 ? "Strong alignment" : score >= 60 ? "Promising, with clear gaps" : score >= 40 ? "Partial alignment" : "Significant gaps",
                skillResults, keywordResults, requirementResults, suggestions, learning,
                "Structured local analysis v1 · skills, phrases, qualifications, responsibilities, and resume evidence",
                "A match score is a planning aid, not an ATS result or hiring prediction. Add a claim only when it is true and you can support it with evidence.", ConceptMatcher.compare(jobText, resumeText), Arrays.stream(resumeText.split("(?:\\R+|(?<=[.!?])\\s+)")).map(String::trim).filter(line -> line.length() >= 20).flatMap(line -> Pattern.compile(".{1,500}(?:\\s+|$)|.{1,500}").matcher(line).results().map(java.util.regex.MatchResult::group)).limit(80).toList());
    }

    private List<ResumeSuggestion> suggestions(List<SkillAssessment> skillResults, List<KeywordAssessment> keywords,
                                                List<RequirementAssessment> requirements) {
        List<ResumeSuggestion> output = new ArrayList<>();
        skillResults.stream().filter(value -> value.status().equals("PROFILE_ONLY")).limit(4).forEach(value -> output.add(new ResumeSuggestion(
                "Make " + value.skill() + " easier to verify",
                "Add it to the project or experience where you actually used it. Describe what you built, your action, and how you checked the outcome.",
                "Built [project or feature] with " + value.skill() + " to [purpose]; verified it using [test, review, or result].",
                "Use this only if you performed the work and can explain it in an interview.")));
        skillResults.stream().filter(value -> value.status().equals("MISSING")).limit(3).forEach(value -> output.add(new ResumeSuggestion(
                "Build evidence for " + value.skill() + " first",
                "Do not add this as a claimed skill yet. Complete a focused learning task and retain a project, exercise, or assessment as evidence.",
                "After learning: Created [small project] using " + value.skill() + " to [solve a concrete problem].",
                "Add the bullet only after completing and reviewing the work.")));
        keywords.stream().filter(value -> value.status().equals("MISSING")).limit(3).forEach(value -> output.add(new ResumeSuggestion(
                "Reflect “" + value.keyword() + "” naturally",
                "If this describes real work, use it in the relevant project or experience bullet instead of adding it to a keyword list.",
                "[Action verb] [work] with [people or process] to [verifiable outcome].",
                "Skip this suggestion when the keyword does not accurately describe your experience.")));
        requirements.stream().filter(value -> !value.status().equals("CLEARLY_REFLECTED")).limit(2).forEach(value -> output.add(new ResumeSuggestion(
                "Address an important " + value.type().toLowerCase(Locale.ROOT),
                "The resume does not clearly demonstrate: “" + value.text() + "” Add evidence in the most relevant project, education, certification, or experience section if it is true.",
                "Lead with what you did, name the relevant tool or method, and add a result only when you can verify it.",
                "Do not imply a qualification, responsibility, or result you do not have.")));
        if (output.isEmpty()) output.add(new ResumeSuggestion("Make evidence specific", "Your main terms align. Improve credibility by connecting each important skill to a concrete action and outcome.", "[Action] [what you built or analyzed] using [skill], resulting in [verified outcome].", "Never invent metrics; use scope, quality checks, or deliverables when no measured result exists."));
        return output.stream().limit(10).toList();
    }

    private int weightedScore(List<SkillAssessment> skillResults, List<KeywordAssessment> keywords,
                              List<RequirementAssessment> qualifications, List<RequirementAssessment> responsibilities) {
        double totalWeight = 0;
        double earned = 0;
        if (!skillResults.isEmpty()) { totalWeight += 55; earned += 55 * skillResults.stream().mapToDouble(value -> value.status().equals("MATCHED") ? 1 : value.status().equals("PROFILE_ONLY") ? .55 : 0).average().orElse(0); }
        if (!keywords.isEmpty()) { totalWeight += 15; earned += 15 * keywords.stream().filter(value -> value.status().equals("MATCHED")).count() / keywords.size(); }
        if (!qualifications.isEmpty()) { totalWeight += 15; earned += 15 * requirementCoverage(qualifications); }
        if (!responsibilities.isEmpty()) { totalWeight += 15; earned += 15 * requirementCoverage(responsibilities); }
        return totalWeight == 0 ? 0 : (int) Math.round(100 * earned / totalWeight);
    }

    private double requirementCoverage(List<RequirementAssessment> values) {
        return values.stream().mapToDouble(value -> value.status().equals("CLEARLY_REFLECTED") ? 1 : value.status().equals("PARTLY_REFLECTED") ? .5 : 0).average().orElse(0);
    }

    private RequirementAssessment assessRequirement(String type, String text, String resumeLower) {
        List<String> terms = meaningfulTokens(text.toLowerCase(Locale.ROOT)).stream().distinct().toList();
        List<String> reflected = terms.stream().filter(term -> mentionsPhrase(resumeLower, term)).toList();
        double coverage = terms.isEmpty() ? 0 : (double) reflected.size() / terms.size();
        String status = coverage >= .6 ? "CLEARLY_REFLECTED" : coverage >= .28 ? "PARTLY_REFLECTED" : "NOT_CLEAR";
        return new RequirementAssessment(type, text, status, reflected.stream().limit(6).toList());
    }

    private List<String> requirementLines(String text, String type) {
        Pattern pattern = type.equals("QUALIFICATION")
                ? Pattern.compile("(?i)\\b(bachelor|master|degree|diploma|certif|years? of experience|experience with|proficien|knowledge of|familiarity with)\\b")
                : Pattern.compile("(?i)\\b(build|develop|design|implement|maintain|analy[sz]e|collaborate|communicate|test|deploy|manage|create|support|review|monitor|write)\\b");
        return Arrays.stream(text.split("(?:\\R+|(?<=[.!?])\\s+|[•●▪])"))
                .map(value -> value.replaceFirst("^[\\s\\-–—*]+", "").trim())
                .filter(value -> value.length() >= 20 && value.length() <= 360 && pattern.matcher(value).find())
                .distinct().limit(6).toList();
    }

    private List<String> keywordCandidates(String jobLower, Set<String> skills) {
        LinkedHashSet<String> output = new LinkedHashSet<>();
        IMPORTANT_PHRASES.stream().filter(value -> mentionsPhrase(jobLower, value)).forEach(output::add);
        Map<String, Long> counts = meaningfulTokens(jobLower).stream().filter(value -> value.length() >= 5)
                .collect(Collectors.groupingBy(value -> value, LinkedHashMap::new, Collectors.counting()));
        counts.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey).filter(value -> skills.stream().noneMatch(skill -> skill.equalsIgnoreCase(value)))
                .limit(12).forEach(output::add);
        return output.stream().limit(15).toList();
    }

    private List<String> meaningfulTokens(String text) {
        return Arrays.stream(text.replaceAll("[^\\p{L}\\p{N}+#./-]+", " ").split("\\s+"))
                .map(value -> value.replaceAll("^[./-]+|[./-]+$", "").toLowerCase(Locale.ROOT))
                .filter(value -> value.length() > 2 && !STOP_WORDS.contains(value) && !value.matches("\\d+"))
                .toList();
    }

    private boolean mentionsSkill(String text, String skill) {
        List<String> phrases = ALIASES.getOrDefault(skill, List.of(skill));
        return phrases.stream().anyMatch(value -> mentionsPhrase(text, value));
    }

    private int firstSkillIndex(String text, String skill) {
        return ALIASES.getOrDefault(skill, List.of(skill)).stream().mapToInt(value -> phraseIndex(text, value))
                .filter(value -> value >= 0).min().orElse(-1);
    }

    private boolean mentionsPhrase(String text, String phrase) { return phraseIndex(text, phrase) >= 0; }
    private int phraseIndex(String text, String phrase) {
        if (text == null || phrase == null || phrase.isBlank()) return -1;
        var matcher = Pattern.compile("(?i)(?<![\\p{L}\\p{N}_+])" + Pattern.quote(phrase.trim()) + "(?![\\p{L}\\p{N}_+])").matcher(text);
        return matcher.find() ? matcher.start() : -1;
    }

    private String excerpt(String text, int index) {
        if (index < 0) return "";
        int start = Math.max(0, index - 100);
        while (start < index && !Character.isWhitespace(text.charAt(start))) start++;
        while (start < index && Character.isWhitespace(text.charAt(start))) start++;
        int end = Math.min(text.length(), index + 220);
        while (end < text.length() && !Character.isWhitespace(text.charAt(end))) end++;
        String value = text.substring(Math.min(start, index), Math.max(Math.min(end, text.length()), index + 1)).replaceAll("\\s+", " ").trim();
        return (start > 0 ? "…" : "") + value + (end < text.length() ? "…" : "");
    }

    private Resume latestResume(Long userId) {
        return resumes.findAllByUserId(userId).stream().max(Comparator.comparing(Resume::getId))
                .orElseThrow(() -> new IllegalArgumentException("Upload a resume in My Skills first, or paste/upload one here"));
    }

    private void validateJob(String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("Paste the job description");
        if (text.length() < 80) throw new IllegalArgumentException("Paste a fuller job description of at least 80 characters");
        if (text.length() > 30000) throw new IllegalArgumentException("Keep the job description under 30,000 characters");
    }

    private String resolveTitle(String requested, String description) {
        if (requested != null && !requested.isBlank()) return requested.trim().substring(0, Math.min(160, requested.trim().length()));
        String first = Arrays.stream(description.split("\\R")).map(String::trim).filter(value -> !value.isBlank()).findFirst().orElse("Saved job match");
        return first.length() <= 90 ? first : first.substring(0, 87).trim() + "…";
    }

    private View toView(JobMatchAnalysis value) {
        try {
            return new View(value.getId(), value.getJobTitle(), value.getJobDescription(), value.getResumeLabel(), value.getScore(),
                    value.getCreatedAt(), value.getUpdatedAt(), json.readValue(value.getResultJson(), Result.class));
        } catch (JacksonException exception) { throw new IllegalStateException("Unable to read the saved analysis", exception); }
    }
}
