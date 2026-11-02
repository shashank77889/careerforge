package com.careerforge.journey;

import java.util.*;
import org.springframework.stereotype.Component;

/** Editorial learning resources. Time estimates describe our practice milestone, not a provider's course duration. */
@Component
public class LearningCatalog {
    public record Resource(String skill, String url, String practice, int hours, int order, List<String> prerequisites,
                           String provider, String format, boolean reviewed) {}
    private final Map<String, Resource> entries = new LinkedHashMap<>();
    public LearningCatalog() {
        add("Python", "https://docs.python.org/3/tutorial/", "Write a script that validates and summarizes a CSV file. Include three tests and explain your error handling.", 8, 10);
        add("Java", "https://dev.java/learn/", "Build a command-line task manager using classes and collections. Test empty inputs and duplicate tasks.", 8, 10);
        add("HTML", "https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Structuring_content", "Build a semantic portfolio page with accessible headings, landmarks, and a labeled contact form.", 4, 10);
        add("CSS", "https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Styling_basics", "Style a responsive portfolio at mobile and desktop widths. Check keyboard focus and readable contrast.", 6, 20, "HTML");
        add("JavaScript", "https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide", "Build a searchable list that fetches data and handles loading, empty results, and errors.", 8, 30, "HTML", "CSS");
        add("React", "https://react.dev/learn", "Build a small application tracker with reusable components, controlled forms, and persistent state.", 8, 40, "JavaScript");
        add("Git", "https://git-scm.com/book/en/v2", "Create a repository, work on a feature branch, resolve a practice conflict, and write a useful README.", 4, 15);
        add("SQL", "https://www.postgresql.org/docs/current/tutorial.html", "Create three related tables. Write joins and grouped queries, and explain how you handle NULL values.", 6, 20);
        add("Statistics", "https://www.khanacademy.org/math/statistics-probability", "Explore a small dataset: compare mean and median, describe uncertainty, and explain a sampling limitation.", 8, 20);
        add("NumPy", "https://numpy.org/learn/", "Replace a loop-based calculation with array operations. Explain shape, broadcasting, and performance tradeoffs.", 5, 30, "Python");
        add("Pandas", "https://pandas.pydata.org/docs/getting_started/intro_tutorials/", "Clean a public dataset, handle missing values, group results, and document each transformation in a notebook.", 6, 35, "Python");
        add("Data Visualization", "https://matplotlib.org/stable/tutorials/index.html", "Create three labeled charts for a dataset and explain what each reveals and what it cannot establish.", 5, 40, "Pandas");
        add("Machine Learning", "https://developers.google.com/machine-learning/crash-course", "Train a baseline model with separate training and test data. Choose a metric and explain data leakage risks.", 10, 50, "Python", "Statistics", "Pandas");
        add("Scikit-learn", "https://scikit-learn.org/stable/getting_started.html", "Build a preprocessing and model pipeline. Compare cross-validation results with a simple baseline.", 8, 55, "Machine Learning", "NumPy");
        add("Deep Learning", "https://www.tensorflow.org/tutorials", "Train a small neural network. Plot training and validation loss and explain signs of overfitting.", 10, 60, "Machine Learning");
        add("Spring Boot", "https://spring.io/guides/", "Build a validated REST API backed by a database. Add tests for invalid input and unauthorized access.", 8, 40, "Java", "SQL");
        add("REST API", "https://developer.mozilla.org/en-US/docs/Web/HTTP/Overview", "Document a CRUD API with request/response examples, appropriate status codes, and pagination.", 5, 30);
        add("Selenium", "https://www.selenium.dev/documentation/", "Automate a local application's sign-in and form flow using stable selectors and explicit assertions.", 8, 40, "Java");
        add("API Testing", "https://learning.postman.com/docs/getting-started/overview/", "Test a local API's success, validation, and authorization cases. Export a repeatable test collection.", 6, 40, "REST API");
        add("Jest", "https://jestjs.io/docs/getting-started", "Write unit tests for a small utility, including edge cases and async failure paths.", 5, 40, "JavaScript");
        add("Excel", "https://support.microsoft.com/en-us/excel", "Clean a sample spreadsheet, create a pivot summary, and explain a chart. A compatible spreadsheet tool is enough.", 5, 15);
        add("Django", "https://docs.djangoproject.com/en/stable/intro/tutorial01/", "Build a small database-backed application with validated forms and tests for permission boundaries.", 8, 40, "Python", "SQL");
        add("Flask", "https://flask.palletsprojects.com/en/stable/tutorial/", "Create a small API with input validation and automated request tests.", 6, 40, "Python");
        add("Networking", "https://www.cloudflare.com/learning/network-layer/what-is-a-computer-network/", "Diagram a browser request through DNS, TCP, TLS, and HTTP. Explain where common failures occur.", 6, 10);
        add("Linux", "https://linuxjourney.com/", "Practice files, permissions, processes, and logs in a local sandbox. Document useful commands.", 6, 20);
        add("Cybersecurity Fundamentals", "https://portswigger.net/web-security/learning-paths", "Explain authentication, authorization, and input validation using intentionally vulnerable training labs only.", 8, 30, "Networking");
        add("SIEM", "https://documentation.wazuh.com/current/getting-started/", "Inspect sample security logs in a local lab. Write an alert rule and explain potential false positives.", 8, 40, "Linux", "Networking");
        add("Ethical Hacking", "https://portswigger.net/web-security/learning-paths", "Complete an authorized training lab and write a finding with reproduction steps and a remediation.", 8, 50, "Cybersecurity Fundamentals");
        add("Cryptography", "https://cryptohack.org/courses/", "Compare hashing and encryption in a training exercise. Explain password storage and key-management limitations.", 6, 40, "Python");
    }
    private void add(String skill, String url, String practice, int hours, int order, String... prerequisites) {
        entries.put(skill, new Resource(skill, url, practice, hours, order, List.of(prerequisites), provider(url), format(url), true));
    }
    private String provider(String url) {
        if (url.contains("python.org")) return "Python Software Foundation";
        if (url.contains("dev.java")) return "Oracle Java";
        if (url.contains("developer.mozilla.org")) return "MDN Web Docs";
        if (url.contains("react.dev")) return "React";
        if (url.contains("git-scm.com")) return "Git";
        if (url.contains("postgresql.org")) return "PostgreSQL";
        if (url.contains("khanacademy.org")) return "Khan Academy";
        if (url.contains("numpy.org")) return "NumPy";
        if (url.contains("pandas.pydata.org")) return "pandas";
        if (url.contains("matplotlib.org")) return "Matplotlib";
        if (url.contains("developers.google.com")) return "Google for Developers";
        if (url.contains("scikit-learn.org")) return "scikit-learn";
        if (url.contains("tensorflow.org")) return "TensorFlow";
        if (url.contains("spring.io")) return "Spring";
        if (url.contains("selenium.dev")) return "Selenium";
        if (url.contains("postman.com")) return "Postman";
        if (url.contains("jestjs.io")) return "Jest";
        if (url.contains("microsoft.com")) return "Microsoft Support";
        if (url.contains("djangoproject.com")) return "Django";
        if (url.contains("palletsprojects.com")) return "Flask";
        if (url.contains("cloudflare.com")) return "Cloudflare Learning Center";
        if (url.contains("linuxjourney.com")) return "Linux Journey";
        if (url.contains("portswigger.net")) return "PortSwigger Web Security Academy";
        if (url.contains("wazuh.com")) return "Wazuh";
        if (url.contains("cryptohack.org")) return "CryptoHack";
        return "Editorially reviewed source";
    }
    private String format(String url) {
        if (url.contains("portswigger.net") || url.contains("cryptohack.org")) return "Interactive learning labs";
        if (url.contains("khanacademy.org") || url.contains("developers.google.com/machine-learning")) return "Free structured course";
        if (url.contains("git-scm.com/book")) return "Official reference book";
        if (url.contains("cloudflare.com/learning") || url.contains("linuxjourney.com")) return "Technical learning guide";
        return "Official documentation";
    }
    public Resource get(String skill) {
        var resource = entries.get(skill);
        if (resource == null) throw new IllegalArgumentException("No learning resource configured for " + skill);
        return resource;
    }
    public List<Resource> all() { return List.copyOf(entries.values()); }
    public static boolean mentions(String text, String skill) {
        return java.util.regex.Pattern.compile("(?i)(?<![\\p{L}\\p{N}_+])" + java.util.regex.Pattern.quote(skill) + "(?![\\p{L}\\p{N}_+])").matcher(text).find();
    }
}
