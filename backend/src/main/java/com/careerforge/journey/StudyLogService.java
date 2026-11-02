package com.careerforge.journey;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class StudyLogService {
    private final StudyLogRepository logs;
    private final com.careerforge.repository.UserRepository users;
    private final JourneyService journeys;
    public StudyLogService(StudyLogRepository logs, JourneyService journeys, com.careerforge.repository.UserRepository users) { this.users=users; this.logs = logs; this.journeys = journeys; }

    public record View(Long id, String sessionKey, LocalDate date, String skill, int plannedMinutes, int studiedMinutes, boolean completed, Instant updatedAt) {}

    public List<View> list(Long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from) || to.isAfter(from.plusDays(62))) throw new IllegalArgumentException("Choose a valid study-log range of up to 63 days");
        String role = journeys.preferences(userId).targetRole;
        return logs.findAllByUserIdAndRoleAndStudyDateBetweenOrderByStudyDateAscIdAsc(userId, role, from, to).stream().map(this::view).toList();
    }

    @Transactional
    public View save(Long userId, String sessionKey, LocalDate date, String skill, int plannedMinutes, int studiedMinutes, boolean completed) {
        users.lockById(userId).orElseThrow();
        if (sessionKey == null || sessionKey.isBlank() || sessionKey.length() > 180) throw new IllegalArgumentException("Choose a valid timetable session");
        if (date == null || date.isBefore(LocalDate.now().minusYears(1)) || date.isAfter(LocalDate.now().plusDays(1))) throw new IllegalArgumentException("Study time can only be logged for a current or past session");
        if (plannedMinutes < 1 || plannedMinutes > 240 || studiedMinutes < 0 || studiedMinutes > 720) throw new IllegalArgumentException("Study minutes are outside the allowed range");
        var journey = journeys.preferences(userId);
        boolean validSkill = journeys.readiness(userId, journey.targetRole).skills().stream().anyMatch(item -> item.skill().equals(skill));
        if (!validSkill) throw new IllegalArgumentException("Choose a skill from your current career path");
        var entry = logs.findByUserIdAndRoleAndStudyDateAndSessionKey(userId, journey.targetRole, date, sessionKey).orElseGet(StudyLog::new);
        entry.userId = userId; entry.role = journey.targetRole; entry.sessionKey = sessionKey.trim(); entry.studyDate = date; entry.skill = skill;
        entry.plannedMinutes = plannedMinutes; entry.studiedMinutes = studiedMinutes; entry.completed = completed; entry.updatedAt = Instant.now();
        return view(logs.save(entry));
    }

    private View view(StudyLog entry) { return new View(entry.id, entry.sessionKey, entry.studyDate, entry.skill, entry.plannedMinutes, entry.studiedMinutes, entry.completed, entry.updatedAt); }
}
