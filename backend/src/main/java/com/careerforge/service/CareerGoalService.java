package com.careerforge.service;

import com.careerforge.model.CareerGoal;
import com.careerforge.model.User;
import com.careerforge.repository.CareerGoalRepository;
import com.careerforge.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CareerGoalService {

    private final CareerGoalRepository careerGoalRepository;
    private final UserRepository userRepository;

    public CareerGoalService(
            CareerGoalRepository careerGoalRepository,
            UserRepository userRepository) {

        this.careerGoalRepository = careerGoalRepository;
        this.userRepository = userRepository;
    }

    public CareerGoal createGoal(Long userId, CareerGoal careerGoal) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (careerGoal.getId() != null) throw new IllegalArgumentException("New goals cannot include an existing ID");
        if (careerGoal.getTargetRole() == null || careerGoal.getTargetRole().isBlank() || careerGoal.getTargetRole().length() > 255)
            throw new IllegalArgumentException("Describe your goal in 1–255 characters");
        if ((careerGoal.getEmploymentType() != null && careerGoal.getEmploymentType().length() > 255)
            || (careerGoal.getPreferredLocation() != null && careerGoal.getPreferredLocation().length() > 255)
            || (careerGoal.getSkills() != null && careerGoal.getSkills().length() > 1000))
            throw new IllegalArgumentException("One of the goal fields is too long");
        careerGoal.setTargetRole(careerGoal.getTargetRole().trim());
        careerGoal.setUser(user);

        return careerGoalRepository.save(careerGoal);
    }

    public List<CareerGoal> getGoals(Long userId) {

        return careerGoalRepository.findAllByUserId(userId);
    }

    public CareerGoal updateCompletion(
            Long goalId,
            boolean completed) {

        CareerGoal goal = careerGoalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Career goal not found"));

        Ownership.require(goal.getUser().getId());
        goal.setCompleted(completed);

        return careerGoalRepository.save(goal);
    }

    public void deleteGoal(Long goalId) {

        careerGoalRepository.deleteById(goalId);
    }
}

