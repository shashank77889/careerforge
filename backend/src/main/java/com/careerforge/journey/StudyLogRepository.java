package com.careerforge.journey;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.*;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {
    Optional<StudyLog> findByUserIdAndRoleAndStudyDateAndSessionKey(Long userId, String role, LocalDate studyDate, String sessionKey);
    List<StudyLog> findAllByUserIdAndRoleAndStudyDateBetweenOrderByStudyDateAscIdAsc(Long userId, String role, LocalDate from, LocalDate to);
}
