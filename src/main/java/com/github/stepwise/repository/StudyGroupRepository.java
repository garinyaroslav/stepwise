package com.github.stepwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.StudyGroup;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
}
