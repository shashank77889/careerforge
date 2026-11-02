package com.careerforge.jobmatch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobMatchRepository extends JpaRepository<JobMatchAnalysis, Long> {
    List<JobMatchAnalysis> findTop20ByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<JobMatchAnalysis> findByIdAndUserId(Long id, Long userId);
}
