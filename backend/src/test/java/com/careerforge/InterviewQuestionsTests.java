package com.careerforge;

import com.careerforge.interview.InterviewQuestions;
import com.careerforge.journey.JourneyService;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;

class InterviewQuestionsTests {
    @Test void everyRoleHasTwentyDistinctQuestionsWithReviewGuidance() {
        var bank = new InterviewQuestions();
        for (String role : JourneyService.ROLES) {
            var full = bank.forRole(role, 20, Set.of());
            assertEquals(20, full.size());
            assertEquals(20, full.stream().map(InterviewQuestions.Question::prompt).distinct().count());
            assertTrue(full.stream().allMatch(q -> !q.topic().isBlank() && q.reviewPoints().size() >= 2));
            for (int count : new int[]{5, 10}) assertEquals(count, bank.forRole(role, count, Set.of()).size());
        }
    }
    @Test void newShortSessionPrioritizesUnseenQuestionsWithinEachCategory() {
        var bank = new InterviewQuestions();
        var first = bank.forRole("Frontend Developer", 5, Set.of());
        var seen = first.stream().map(InterviewQuestions.Question::prompt).collect(Collectors.toSet());
        assertTrue(bank.forRole("Frontend Developer", 5, seen).stream().noneMatch(q -> seen.contains(q.prompt())));
    }
}
