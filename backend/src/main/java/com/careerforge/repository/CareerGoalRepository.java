package com.careerforge.repository;

import com.careerforge.model.CareerGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerGoalRepository
        extends JpaRepository<CareerGoal, Long> {

    List<CareerGoal> findAllByUserId(Long userId);
}