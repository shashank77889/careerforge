package com.careerforge.repository;

import com.careerforge.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository
        extends JpaRepository<Resume, Long> {

    List<Resume> findAllByUserId(Long userId);
}