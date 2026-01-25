package com.github.stepwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.WorkTemplate;

public interface WorkTemplateRepository extends JpaRepository<WorkTemplate, Long> {
}
