package com.github.stepwise.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.User;
import com.github.stepwise.service.ProjectService;
import com.github.stepwise.web.dto.ExplanatoryNoteItemResponseDto;
import com.github.stepwise.web.dto.ProjectResponseDto;
import com.github.stepwise.web.dto.UpdateProjectDto;
import com.github.stepwise.web.dto.UserResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
@Slf4j
public class ProjectController {

  private final ProjectService projectService;

  @PutMapping
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<UpdateProjectDto> updateProject(@RequestBody UpdateProjectDto projectDto) {
    log.info("Updating project with id: {}", projectDto.getId());

    Project updatedProject = projectService.updateProject(
        new Project(projectDto.getId(), projectDto.getTitle(), projectDto.getDescription()));

    return new ResponseEntity<UpdateProjectDto>(new UpdateProjectDto(updatedProject.getId(),
        updatedProject.getTitle(), updatedProject.getDescription()), HttpStatus.OK);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
  public ResponseEntity<ProjectResponseDto> getStudentProject(@RequestParam Long projectId) {
    log.info("Fetching project, project id: {}", projectId);

    Project project = projectService.getByProjectId(projectId);

    User student = project.getStudent();

    UserResponseDto owner = new UserResponseDto(student.getId(), student.getUsername(), student.getEmail(),
        student.getProfile().getFirstName(), student.getProfile().getLastName(), student.getProfile().getMiddleName());

    List<ExplanatoryNoteItemResponseDto> items = project.getItems().stream()
        .map(item -> new ExplanatoryNoteItemResponseDto(item.getId(), item.getOrderNumber(), item.getStatus(),
            item.getFileName(), item.getTeacherComment(), item.getDraftedAt(), item.getSubmittedAt(),
            item.getApprovedAt(), item.getRejectedAt()))
        .toList();

    ProjectResponseDto projectDto = new ProjectResponseDto(project.getId(), project.getTitle(),
        project.getDescription(), owner, items, project.isApprovedForDefense());

    return new ResponseEntity<>(projectDto, HttpStatus.OK);
  }

  @GetMapping("/work/{workId}")
  @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
  public ResponseEntity<List<ProjectResponseDto>> getProjectsByWork(@PathVariable Long workId) {
    log.info("Fetching projects by work id: {}", workId);

    List<Project> projects = projectService.getAllByWorkId(workId);

    List<ProjectResponseDto> projectDtos = new ArrayList<>(35);

    for (Project project : projects) {
      User student = project.getStudent();

      UserResponseDto owner = new UserResponseDto(student.getId(), student.getUsername(), student.getEmail(),
          student.getProfile().getFirstName(), student.getProfile().getLastName(),
          student.getProfile().getMiddleName());

      List<ExplanatoryNoteItemResponseDto> items = project.getItems().stream()
          .map(item -> new ExplanatoryNoteItemResponseDto(item.getId(), item.getOrderNumber(), item.getStatus(),
              item.getFileName(), item.getTeacherComment(), item.getDraftedAt(), item.getSubmittedAt(),
              item.getApprovedAt(), item.getRejectedAt()))
          .toList();

      ProjectResponseDto projectDto = new ProjectResponseDto(project.getId(), project.getTitle(),
          project.getDescription(), owner, items, project.isApprovedForDefense());

      projectDtos.add(projectDto);
    }

    return new ResponseEntity<>(projectDtos, HttpStatus.OK);
  }

}
