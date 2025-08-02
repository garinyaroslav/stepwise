package com.github.stepwise.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.stepwise.entity.Project;
import com.github.stepwise.service.ProjectService;
import com.github.stepwise.web.dto.UpdateProjectDto;
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


}

