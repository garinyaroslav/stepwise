package com.github.stepwise.service;

import java.util.List;

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

  public Project getByProjectId(Long projectId) {
    log.info("Fetching project id: {}", projectId);

    return projectRepository.findById(projectId).orElseThrow(() -> new IllegalArgumentException(
        "Project not found project id: " + projectId));
  }

  public List<Project> getAllByWorkId(Long workId) {
    log.info("Fetching all projects for work id: {}", workId);

    return projectRepository.findAllByAcademicWorkId(workId);
  }

}
