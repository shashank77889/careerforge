package com.careerforge.journey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LearningStepRepository extends JpaRepository<LearningStep, Long> {
    List<LearningStep> findAllByUserIdAndRoleOrderByIdAsc(Long userId, String role);
}
