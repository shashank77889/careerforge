package com.careerforge.interview;

import com.careerforge.journey.JourneyService;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class InterviewQuestions {
    public record Question(String topic, String prompt, List<String> reviewPoints, List<String> options, Integer correctIndex, String explanation) {
        public Question(String topic,String prompt,List<String> reviewPoints) { this(topic,prompt,reviewPoints,List.of(),null,null); }
    }
    private final Map<String, List<Question>> bank = new LinkedHashMap<>();
    private Question q(String topic, String prompt, String... points) { return new Question(topic,prompt,List.of(points)); }
    public InterviewQuestions() {
        bank.put("Data Scientist",List.of(
            q("Statistics","A product team says a new feature improved conversion from 4% to 5%. What would you ask before agreeing?","Check sample sizes, assignment, and experiment duration.","Discuss uncertainty, significance, and practical importance.","Look for confounders, multiple comparisons, and segment differences."),
            q("Pandas","A dataset contains missing values, duplicates, and inconsistent dates. Walk through your cleaning process.","Profile the data and understand why values are missing.","Explain how you identify duplicates and normalize dates.","Keep transformations reproducible and check whether cleaning changes conclusions."),
            q("Machine Learning","Your model performs well during training but poorly on new data. How would you investigate?","Check train/test separation, leakage, and preprocessing.","Compare a baseline and inspect overfitting and distribution shifts.","Choose evaluation metrics appropriate to the problem."),
            q("Communication","Describe how you would present a model's results to someone who does not work with data.","Start with the decision or problem, not the algorithm.","Use a clear comparison or visualization.","Explain uncertainty, limitations, and the recommended next step.")
        ));
        bank.put("Data Analyst",List.of(
            q("SQL","You need revenue per customer, including customers with no orders. How would you structure the query?","Choose a left join and an appropriate grouping key.","Handle NULL values and avoid duplicate counting.","Check the result against a small known example."),
            q("Data quality","Two dashboards report different totals for the same month. How would you reconcile them?","Compare filters, definitions, date boundaries, and time zones.","Trace data sources, joins, and refresh schedules.","Agree on a documented metric definition."),
            q("Visualization","Which chart would you use to explain a trend over time, and how would you avoid misleading the audience?","Choose a clear time-based chart with consistent intervals.","Label units and axes; explain missing data.","Avoid unsupported causal claims."),
            q("Statistics","Averages rose this month but most customers seem worse off. What could explain this?","Compare mean, median, and the distribution.","Segment the data and check outliers or composition changes.","Explain what additional data would test your hypothesis.")
        ));
        bank.put("AI/ML Engineer",List.of(
            q("ML systems","How would you move a trained model from a notebook into a reliable service?","Package reproducible preprocessing and inference.","Version data and models; test latency and failure handling.","Monitor quality and provide a rollback strategy."),
            q("Evaluation","A classifier has 98% accuracy on an imbalanced dataset. Is it useful?","Compare against a majority-class baseline.","Discuss precision, recall, and error costs.","Check evaluation data quality and calibration."),
            q("Deep learning","Training loss falls while validation loss rises. What would you try?","Explain overfitting and verify the validation split.","Consider regularization, early stopping, and simpler models.","Change one variable at a time and track experiments."),
            q("Responsible AI","How would you evaluate an AI feature before exposing it to users?","Define representative tests and failure cases.","Check privacy, harmful outputs, and uneven performance.","Plan monitoring, user feedback, and human escalation.")
        ));
        bank.put("SDET",List.of(
            q("Test strategy","How would you test a registration form beyond the happy path?","Cover validation, duplicate accounts, and boundaries.","Test authorization, data handling, and accessibility.","Separate unit, API, and end-to-end coverage."),
            q("Selenium","A browser test fails intermittently in CI. How would you diagnose it?","Inspect logs, screenshots, network timing, and test isolation.","Use stable selectors and condition-based waits.","Avoid hiding genuine defects with retries alone."),
            q("API testing","What would you test for an endpoint that updates an application status?","Check valid transitions and malformed requests.","Check authentication and cross-user authorization.","Verify persistence and relevant error responses."),
            q("Debugging","A developer cannot reproduce a bug you reported. How would you help?","Provide minimal steps, environment, and test data.","Separate observed behavior from assumptions.","Include expected behavior, evidence, and impact.")
        ));
        bank.put("Java Developer",List.of(
            q("Java collections","When would you choose a List, Set, or Map, and what mistakes could arise?","Explain ordering, uniqueness, and key lookup.","Discuss equality and hashCode contracts.","Consider complexity, nulls, and thread safety."),
            q("Spring Boot","How would you validate and handle errors in a REST endpoint?","Use explicit request validation and clear status codes.","Keep business rules in a service layer.","Return safe error messages and test invalid requests."),
            q("Transactions","A workflow creates two related records. What happens if the second write fails?","Explain atomicity and transaction boundaries.","Ensure rollback rather than leaving an orphan record.","Consider retries, uniqueness, and idempotency."),
            q("Testing","How would you test a service that depends on a database and an external API?","Separate unit tests from integration tests.","Control external responses and failure modes.","Verify important persistence behavior with a test database.")
        ));
        bank.put("Python Developer",List.of(
            q("Python","Explain mutable defaults in Python functions and how you would avoid problems.","Explain when default arguments are evaluated.","Show why state may leak between calls.","Use a sentinel such as None and create a fresh value inside."),
            q("Web APIs","How would you design a Python endpoint that accepts user input and writes to a database?","Validate and normalize input.","Authenticate and authorize the caller.","Handle transactions, errors, and tests."),
            q("Performance","A Python script is slow on a large file. How would you improve it?","Measure and profile before optimizing.","Consider streaming rather than loading everything.","Check algorithmic complexity and suitable libraries."),
            q("Reliability","How would you handle a third-party API that sometimes times out?","Set timeouts and bounded retries where safe.","Consider idempotency and backoff.","Provide clear failure behavior and useful logs.")
        ));
        bank.put("Frontend Developer",List.of(
            q("React","A page fetches data when a filter changes. How would you prevent stale results and confusing loading states?","Cancel or ignore stale requests.","Model loading, error, empty, and success states explicitly.","Keep effect dependencies correct and avoid unnecessary fetches."),
            q("Accessibility","How would you make a modal or form usable with a keyboard and screen reader?","Use semantic elements and associated labels.","Manage focus and keyboard interactions.","Announce useful errors and verify contrast and zoom behavior."),
            q("CSS","How would you build a layout that works on a small phone and a wide desktop?","Use flexible layout and content-driven breakpoints.","Handle long text, overflow, and touch targets.","Test realistic viewport sizes and zoom."),
            q("State","Which state belongs in a component, a shared store, the URL, or the server?","Explain ownership and persistence requirements.","Use URLs for shareable navigation/filter state when appropriate.","Keep server data synchronized and avoid conflicting copies.")
        ));
        bank.put("Backend Developer",List.of(
            q("API design","How would you design an endpoint that safely updates a user's career goal?","Authenticate and check record ownership.","Validate inputs and handle missing records.","Use an appropriate method and test persistence."),
            q("Concurrency","Two requests try to update the same record at once. What options do you have?","Explain lost updates and optimistic locking.","Discuss transactions or locking where needed.","Return a clear conflict and design a retry strategy."),
            q("Databases","A database-backed endpoint is getting slower as data grows. What would you inspect?","Inspect query plans and indexes.","Check unnecessary queries and pagination.","Measure the impact of any changes."),
            q("Security","How do authentication and authorization differ in a JWT-based API?","Authentication identifies the caller; authorization checks permitted actions.","Validate signatures and expiration.","Check ownership on every sensitive resource operation.")
        ));
        bank.put("Cybersecurity Analyst",List.of(
            q("Triage","You see repeated failed logins followed by a successful login. How would you investigate?","Establish context and correlate timestamps, sources, and account activity.","Consider benign explanations as well as compromise.","Document evidence and follow an authorized escalation process."),
            q("Networking","Explain what happens when a browser opens an HTTPS site and where you might inspect a failure.","Describe DNS, connection establishment, TLS, and HTTP.","Distinguish encryption from endpoint trust.","Use authorized logs and diagnostic tools."),
            q("SIEM","An alert rule produces too many false positives. How would you improve it?","Review representative alerts and establish a baseline.","Tune with context rather than suppressing all alerts.","Measure missed detections as well as false positives."),
            q("Incident response","What should you do after finding a potential vulnerability in a system?","Stay within authorized scope and avoid unnecessary impact.","Preserve evidence and report through the agreed channel.","Describe risk, remediation, and how to verify the fix.")
        ));
        if (!bank.keySet().containsAll(JourneyService.ROLES)) throw new IllegalStateException("Missing interview role coverage");
    }
    public List<Question> forRole(String role) {
        return forRole(role, 10, Set.of());
    }
    public List<Question> forRole(String role, int count, Set<String> recentlyAsked) {
        if (!bank.containsKey(role)) throw new IllegalArgumentException("Choose a supported interview role");
        if (!List.of(5, 10, 20).contains(count)) throw new IllegalArgumentException("Choose a session of 5, 10, or 20 questions");
        var extra = new InterviewPracticeBank();
        var questions = new ArrayList<>(bank.get(role));
        questions.addAll(extra.technical(role));
        var general = new ArrayList<>(extra.general());
        // Shuffle ties, then prefer questions not included in recent sessions.
        Collections.shuffle(questions);
        Collections.shuffle(general);
        questions.sort(Comparator.comparing(q -> recentlyAsked.contains(q.prompt())));
        general.sort(Comparator.comparing(q -> recentlyAsked.contains(q.prompt())));
        int technicalCount = count == 5 ? 4 : count == 10 ? 7 : 10;
        var selected = new ArrayList<>(questions.subList(0, technicalCount));
        selected.addAll(general.subList(0, count - technicalCount));
        return List.copyOf(selected);
    }
}
