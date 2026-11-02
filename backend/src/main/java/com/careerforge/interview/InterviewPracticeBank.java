package com.careerforge.interview;

import java.util.*;
import static com.careerforge.interview.InterviewQuestions.Question;

final class InterviewPracticeBank {
    private Question q(String topic, String prompt, String... points) { return new Question(topic, prompt, List.of(points)); }
    List<Question> technical(String role) {
        return switch (role) {
            case "Data Scientist" -> role0();
            case "Data Analyst" -> role1();
            case "AI/ML Engineer" -> role2();
            case "SDET" -> role3();
            case "Java Developer" -> role4();
            case "Python Developer" -> role5();
            case "Frontend Developer" -> role6();
            case "Backend Developer" -> role7();
            case "Cybersecurity Analyst" -> role8();
            default -> throw new IllegalArgumentException("Choose a supported interview role");
        };
    }
    List<Question> general() { return common(); }
    private List<Question> role0() {
        return List.of(
            q("SQL", "How would you find the top three products by revenue within each category?", "Aggregate at product level before ranking.", "Explain ties and partitioned window functions."),
            q("Experiment design", "Design an experiment to test whether a reminder improves course completion.", "Define a primary metric, randomization unit, and guardrails.", "Discuss sample size, duration, and interference between users."),
            q("Feature engineering", "How would you encode categorical features without leaking information?", "Fit transformations on training data only.", "Handle unseen categories and validate with an appropriate split."),
            q("Model choice", "When would you prefer a simple regression model over a complex ensemble?", "Compare interpretability, data size, and predictive performance.", "Use a baseline and evaluate the cost of additional complexity."),
            q("Time series", "How would you evaluate a forecast for next month's demand?", "Use time-ordered validation and a seasonal baseline.", "Explain horizon-specific errors and avoid future information."),
            q("Bias", "Your model works poorly for a small user segment. How would you investigate?", "Compare sample sizes, labels, and error distributions.", "Propose additional data or model changes and reevaluate by segment.")
        );
    }
    private List<Question> role1() {
        return List.of(
            q("SQL windows", "How would you calculate each customer's running total of spending?", "Partition by customer and define a stable ordering.", "Explain window frames and duplicate timestamps."),
            q("Spreadsheets", "How would you audit a spreadsheet with thousands of formulas?", "Check input types, references, missing values, and duplicate rows.", "Reconcile sample calculations and document assumptions."),
            q("Business metrics", "A team wants to measure student engagement. What metric would you propose?", "Connect the measure to a specific decision.", "Distinguish activity from meaningful outcomes and define guardrails."),
            q("Experiment analysis", "How would you analyze a test where the treatment group has fewer users?", "Inspect assignment and missing data before comparing outcomes.", "Quantify uncertainty and avoid declaring a winner from raw totals."),
            q("Data modeling", "How would you structure orders, customers, and products for reporting?", "State the grain and relationships of each table.", "Prevent many-to-many joins from inflating totals."),
            q("Python analysis", "How would you automate a recurring CSV report?", "Validate schemas and make transformations reproducible.", "Add checks, failure messages, and a documented output.")
        );
    }
    private List<Question> role2() {
        return List.of(
            q("Data pipelines", "How would you keep training features and online features consistent?", "Share versioned transformation definitions.", "Test point-in-time correctness and monitor feature distributions."),
            q("Retrieval", "Design a retrieval-based assistant for a set of course documents.", "Explain chunking, retrieval, and source attribution.", "Evaluate answer grounding and behavior when evidence is missing."),
            q("Inference performance", "How would you reduce inference latency without unacceptable quality loss?", "Measure bottlenecks and latency percentiles.", "Compare batching, caching, quantization, and quality tradeoffs."),
            q("Retraining", "What signals should trigger investigation or retraining of a model?", "Separate data drift, concept drift, and service failures.", "Use delayed labels and controlled rollout with rollback."),
            q("Reproducibility", "Another engineer cannot reproduce your training result. What do you check?", "Record datasets, code, environment, seeds, and configuration.", "Explain nondeterminism and compare artifacts."),
            q("Model testing", "What tests would you add to an ML inference API?", "Check input schemas, edge cases, and preprocessing consistency.", "Test load, failures, representative quality, and output contracts.")
        );
    }
    private List<Question> role3() {
        return List.of(
            q("Test design", "How would you derive test cases for a password length rule?", "Use equivalence classes and boundary values.", "Check empty, Unicode, and maximum-length inputs."),
            q("CI/CD", "Which tests should run on each commit and which can run later?", "Balance feedback speed with risk coverage.", "Discuss isolation, failure ownership, and release gates."),
            q("Performance testing", "How would you load-test a job search endpoint?", "Use a realistic workload and measurable latency/error targets.", "Identify bottlenecks and distinguish load from stress testing."),
            q("Test data", "How would you prevent parallel automated tests from interfering?", "Create isolated data and predictable setup/cleanup.", "Avoid order dependencies and shared mutable accounts."),
            q("Automation design", "How would you keep a large UI test suite maintainable?", "Separate page interactions from assertions and test intent.", "Use stable selectors and avoid excessive abstraction."),
            q("Coverage", "All tests pass but users report defects. What would you change?", "Map coverage to risk and real workflows.", "Add exploratory testing and regressions based on root causes.")
        );
    }
    private List<Question> role4() {
        return List.of(
            q("Object design", "When would you use composition instead of inheritance?", "Explain substitutability and coupling with a concrete example.", "Show how the choice affects testing and future changes."),
            q("Concurrency", "How would you make a shared counter safe across threads?", "Explain the race condition and visibility.", "Compare atomic operations and synchronization."),
            q("Exceptions", "How would you decide where to catch an exception in a Java application?", "Preserve useful context and avoid silently swallowing failures.", "Separate recoverable conditions from programming errors."),
            q("JPA", "What causes the N+1 query problem and how would you address it?", "Describe lazy relationship loading and query counts.", "Compare fetch joins, projections, and pagination tradeoffs."),
            q("Streams", "When is a stream pipeline clearer than a loop, and when is it risky?", "Discuss side effects, readability, and short-circuiting.", "Avoid assuming parallel streams always improve performance."),
            q("Memory", "A Java service's memory usage keeps growing. How would you investigate?", "Inspect heap usage, allocation patterns, and retained objects.", "Distinguish leaks from expected caching and verify the fix.")
        );
    }
    private List<Question> role5() {
        return List.of(
            q("Iterators", "When would you use a generator instead of a list?", "Explain lazy evaluation and memory usage.", "Discuss one-pass iteration and resource lifetime."),
            q("Concurrency", "How would you choose between asyncio, threads, and processes?", "Distinguish I/O-bound from CPU-bound work.", "Discuss blocking code, overhead, and shared state."),
            q("Data modeling", "How would you choose between a dictionary, dataclass, and a custom class?", "Compare structure, validation, and behavior.", "Explain readability and serialization needs."),
            q("Testing", "How would you test code that reads files and calls an external service?", "Use temporary files and controlled service responses.", "Test malformed inputs, timeouts, and cleanup."),
            q("Packaging", "How would you make a Python project reproducible for another developer?", "Specify dependencies and a supported Python version.", "Document setup, configuration, and test commands."),
            q("SQL", "How would you prevent SQL injection in a Python API?", "Use parameterized queries rather than string interpolation.", "Validate identifiers separately and apply least privilege.")
        );
    }
    private List<Question> role6() {
        return List.of(
            q("JavaScript", "Explain how promises and the event loop affect the order of UI updates.", "Distinguish synchronous work from queued callbacks.", "Use a small example and discuss blocking the main thread."),
            q("Performance", "A page takes too long to become interactive. How would you investigate?", "Measure network, bundle, rendering, and main-thread costs.", "Prioritize measured improvements and verify user impact."),
            q("Testing", "How would you test a search form with debounced API requests?", "Test user-visible behavior and control timers/network responses.", "Cover stale responses, empty results, and failures."),
            q("Security", "How would you safely render content supplied by another user?", "Prefer text rendering and avoid unsafe HTML insertion.", "Discuss sanitization where HTML is required and server-side controls."),
            q("Forms", "How would you design a form that preserves work after a failed save?", "Keep drafts and distinguish validation from network errors.", "Provide accessible feedback and safe retry behavior."),
            q("TypeScript", "How would you model loading, success, and failure states in TypeScript?", "Use a discriminated union with explicit state-specific fields.", "Explain exhaustive handling and runtime input validation.")
        );
    }
    private List<Question> role7() {
        return List.of(
            q("Caching", "What would you cache in a job listing API and how would you invalidate it?", "Define keys, TTLs, and consistency requirements.", "Avoid leaking user-specific data and handle cache failure."),
            q("Queues", "How would you process resume analysis asynchronously?", "Define job states, retries, and failure visibility.", "Make processing idempotent and handle duplicate delivery."),
            q("Reliability", "A downstream service is failing. How should your API respond?", "Set timeouts and bounded retries with backoff.", "Consider circuit breaking, graceful degradation, and observability."),
            q("Pagination", "How would you paginate a large, frequently changing list of jobs?", "Compare offset and cursor pagination.", "Use stable ordering and explain consistency tradeoffs."),
            q("Observability", "Which logs and metrics would help diagnose an intermittent slow endpoint?", "Correlate requests and inspect latency percentiles, errors, and traces.", "Avoid logging secrets or unnecessary personal data."),
            q("Schema changes", "How would you deploy a database schema change without breaking existing clients?", "Plan backward-compatible expansion and data migration.", "Verify rollout order, constraints, and rollback options.")
        );
    }
    private List<Question> role8() {
        return List.of(
            q("Access control", "How would you review permissions for a newly deployed application?", "Check least privilege and separation of duties.", "Verify ownership checks and audit privileged activity."),
            q("Phishing", "A student reports a suspicious email. How would you assess and respond?", "Inspect indicators safely without opening risky links.", "Preserve evidence and follow the reporting and containment process."),
            q("Vulnerability management", "How would you prioritize a long list of reported vulnerabilities?", "Combine severity with exposure, exploitability, and asset importance.", "Track owners, mitigations, and verification."),
            q("Log analysis", "How would you investigate unusual outbound traffic from a workstation?", "Correlate destination, process, timing, and baseline activity.", "Preserve evidence and seek authorized containment."),
            q("Cloud security", "What would you check after discovering a publicly accessible storage bucket?", "Verify intended access and scope of exposed data.", "Restrict access through the authorized process and review audit logs."),
            q("Threat modeling", "How would you threat-model a resume upload feature?", "Identify assets, trust boundaries, and untrusted file handling.", "Discuss access controls, size/type validation, and safe processing.")
        );
    }
    private List<Question> common() {
        return List.of(
            q("Introduction", "Give a 90-second introduction connecting your background to this role.", "Choose two relevant experiences, including coursework if needed.", "Explain what you contributed and what you want to learn."),
            q("Project walkthrough", "Walk through a project from problem definition to delivery.", "Describe requirements, design choices, and your own contribution.", "Show evidence and acknowledge limitations."),
            q("Project tradeoffs", "Describe a technical decision in your project and an alternative you rejected.", "Explain constraints and the comparison you made.", "Discuss when the alternative would be preferable."),
            q("Debugging process", "Describe how you would investigate an unfamiliar failure with limited information.", "Gather reproducible evidence and test hypotheses one at a time.", "Explain when to ask for help and how to verify a fix."),
            q("Behavioral", "Tell me about a difficult learning or project challenge. What did you do and what changed?", "Use a real situation and explain your personal actions.", "Describe the outcome and what you would do differently."),
            q("Collaboration", "Tell me about a disagreement over a project approach. How did you handle it?", "Explain how you listened and compared evidence.", "Describe the decision and what you learned; a classroom example is valid."),
            q("Prioritization", "You have two days to finish a project and several unfinished features. What do you do?", "Prioritize user needs, dependencies, and failure risks.", "Communicate scope changes and preserve time for validation."),
            q("Learning", "You are asked about a technology you have never used. How would you respond?", "Be clear about what you do and do not know.", "Connect relevant foundations and propose a concrete learning plan."),
            q("Feedback", "Describe feedback that changed how you approached your work.", "Use a specific example and explain the change you made.", "Show how you checked whether it helped."),
            q("Closing questions", "What would you ask the interviewer to understand the role and team?", "Ask about responsibilities, support, and expectations for early success.", "Connect questions to the actual role rather than memorizing a script.")
        );
    }
}
