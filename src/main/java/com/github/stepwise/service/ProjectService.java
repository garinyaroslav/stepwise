package com.github.stepwise.service;

import java.util.List;

import org.springframework.stereotype.Service;
import com.github.stepwise.entity.ItemStatus;
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

    return projectRepository.findById(projectId).orElseThrow(
        () -> new IllegalArgumentException("Project not found project id: " + projectId));
  }

  public List<Project> getAllByWorkId(Long workId) {
    log.info("Fetching all projects for work id: {}", workId);

    return projectRepository.findAllByAcademicWorkId(workId);
  }

  public List<Project> getAllByWorkIdAndStudentId(Long workId, Long studentId) {
    log.info("Fetching all projects for work id: {}, and student id: {}", workId, studentId);

    return projectRepository.findAllByAcademicWorkIdAndStudentId(workId, studentId);
  }

  public Project approve(Long projectId) {
    log.info("Approving project with id: {}", projectId);

    Project project = projectRepository.findById(projectId).orElseThrow(() -> {
      log.error("Project with id {} not found", projectId);
      return new IllegalArgumentException("Project not found with id: " + projectId);
    });

    int approvedItemsCount = (int) project.getItems().stream()
        .filter(item -> item.getStatus() == ItemStatus.APPROVED).count();
    int workItemsCount = project.getAcademicWork().getAcademicWorkChapters().size();

    if (approvedItemsCount != workItemsCount) {
      log.error("Cannot approve project with id: {}. Not all items are approved.", projectId);
      throw new IllegalArgumentException("Cannot approve project with id: " + projectId);
    }

    project.setApprovedForDefense(true);

    Project updatedProject = projectRepository.save(project);

    log.info("Project with id: {} approved successfully", updatedProject.getId());

    return updatedProject;
  }

  public boolean isProjectBelongsToStudent(Long projectId, Long studentId) {
    log.info("Checking if project with id: {} belongs to student with id: {}", projectId,
        studentId);

    return projectRepository.existsByIdAndStudentId(projectId, studentId);
  }

  public boolean isProjectBelongsToTeacher(Long projectId, Long teacherId) {
    log.info("Checking if project with id: {} belongs to teacher with id: {}", projectId,
        teacherId);

    return projectRepository.existsByIdAndTeacherId(projectId, teacherId);
  }


}
