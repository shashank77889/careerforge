package com.careerforge.jobmatch;

import java.util.*;
import java.util.regex.Pattern;

/** Transparent ontology-based semantic matching; not a trained embedding model. */
public final class ConceptMatcher {
    private ConceptMatcher() {}
    private static final Map<String, List<String>> CONCEPTS = Map.ofEntries(
        Map.entry("Automated testing", List.of("automated testing", "automated tests", "test automation", "regression suite", "automated checks")),
        Map.entry("Data cleaning", List.of("data cleaning", "data cleansing", "clean datasets", "removed duplicates", "handled missing values")),
        Map.entry("Data visualization", List.of("data visualization", "data visualisation", "interactive charts", "visual dashboards", "plotted trends")),
        Map.entry("API development", List.of("api development", "restful services", "rest endpoints", "rest apis", "http endpoints")),
        Map.entry("Continuous delivery", List.of("continuous delivery", "ci/cd", "deployment pipeline", "automated deployments")),
        Map.entry("Team collaboration", List.of("team collaboration", "cross-functional collaboration", "worked with designers", "collaborated with engineers")),
        Map.entry("Model evaluation", List.of("model evaluation", "cross validation", "cross-validation", "evaluated precision and recall", "evaluated model performance")),
        Map.entry("Database optimization", List.of("database optimization", "query optimization", "optimized queries", "improved query performance")),
        Map.entry("Version control", List.of("version control", "git workflows", "pull requests", "git branches")),
        Map.entry("User research", List.of("user research", "usability testing", "interviewed users", "conducted user interviews")),
        Map.entry("Access control", List.of("access control", "role-based permissions", "role based access", "authorization rules")),
        Map.entry("Incident response", List.of("incident response", "security incident investigation", "investigated security alerts", "triaged security incidents"))
    );
    public record Evidence(String concept, String requirement, String resumeEvidence, String status) {}
    public record Comparison(String version, String method, int requiredConcepts, int supportedConcepts,
                             List<Evidence> evidence, String limitation) {}
    private static boolean contains(String text, String phrase) {
        return Pattern.compile("(?iu)(?<![\\p{L}\\p{N}])" + Pattern.quote(phrase) + "(?![\\p{L}\\p{N}])").matcher(text).find();
    }
    private static List<String> sentences(String text) {
        return Arrays.stream(text.split("(?:\\R+|(?<=[.!?])\\s+)"))
            .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
    private static boolean positive(String sentence) {
        return !Pattern.compile("(?i)\\b(no|not|never|without|lack|lacking|learning|learn|plan|planned|hope|aspire|want)\\b").matcher(sentence).find();
    }
    public static Comparison compare(String job, String resume) {
        var evidence = new ArrayList<Evidence>();
        var jobLines = sentences(job);
        var resumeLines = sentences(resume);
        CONCEPTS.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            String requirement = jobLines.stream().filter(line -> entry.getValue().stream().anyMatch(p -> contains(line, p))).findFirst().orElse(null);
            if (requirement == null) return;
            String support = resumeLines.stream().filter(ConceptMatcher::positive)
                .filter(line -> entry.getValue().stream().anyMatch(p -> contains(line, p))).findFirst().orElse("");
            evidence.add(new Evidence(entry.getKey(), requirement, support, support.isEmpty() ? "NOT_FOUND" : "REVIEW_EVIDENCE"));
        });
        return new Comparison("concept-v1", "Curated concept dictionary", evidence.size(),
            (int)evidence.stream().filter(e -> !e.resumeEvidence().isEmpty()).count(), evidence,
            "Limited to 12 curated concepts. Related wording is a review cue, not proof of proficiency. Negated or future-learning claims are excluded conservatively. This is not a trained language model and does not alter the keyword score.");
    }
}
