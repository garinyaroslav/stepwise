package com.github.stepwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  List<Project> findAllByAcademicWorkId(Long academicWorkId);

}
