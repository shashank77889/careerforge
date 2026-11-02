package com.careerforge.journey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SnapshotRepository extends JpaRepository<ReadinessSnapshot, Long> {
    List<ReadinessSnapshot> findTop12ByUserIdAndRoleOrderByRecordedAtDesc(Long userId, String role);
}
