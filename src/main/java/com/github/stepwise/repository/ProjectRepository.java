package com.github.stepwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
