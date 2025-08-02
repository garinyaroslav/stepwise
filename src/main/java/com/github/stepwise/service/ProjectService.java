package com.github.stepwise.service;

import org.springframework.stereotype.Service;
import com.github.stepwise.entity.Project;
import com.github.stepwise.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

  private final ProjectRepository projectRepository;

  public Project updateProject(Project newProject) {
    log.info("Updating project with id: {}, {}", newProject.getId(), newProject);

    Project project = projectRepository.findById(newProject.getId()).orElseThrow(() -> {
      log.error("Project with id {} not found", newProject.getId());
      return new IllegalArgumentException("Project not found with id: " + newProject.getId());
    });

    project.setTitle(newProject.getTitle());
    project.setDescription(newProject.getDescription());

    Project updatedProject = projectRepository.save(project);

    return updatedProject;
  }


}
