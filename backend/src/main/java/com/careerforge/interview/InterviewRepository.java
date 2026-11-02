package com.careerforge.interview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface InterviewRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findTop30ByUserIdOrderByStartedAtDesc(Long userId);
}
