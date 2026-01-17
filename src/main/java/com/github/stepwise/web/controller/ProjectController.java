package com.github.stepwise.web.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.ProjectService;
import com.github.stepwise.web.dto.ProjectResponseDto;
import com.github.stepwise.web.dto.UpdateProjectDto;
import jakarta.validation.Valid;
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
    public ResponseEntity<UpdateProjectDto> updateProject(@Valid @RequestBody UpdateProjectDto projectDto) {
        log.info("Updating project with id: {}", projectDto.getId());

        var updatedProject = projectService.updateProject(projectDto.toEntity());

        return ResponseEntity.ok(UpdateProjectDto.fromEntity(updatedProject));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponseDto> getStudentProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching project, project id: {}", projectId);
        AppUserDetails appUserDetails = (AppUserDetails) userDetails;

        if (appUserDetails.getRole() == UserRole.STUDENT &&
                !projectService.isProjectBelongsToStudent(projectId, appUserDetails.getId())) {

            log.error("Project with id: {} does not belong to student with id: {}",
                    projectId, appUserDetails.getId());
            throw new IllegalArgumentException("Project not found with id: " + projectId
                    + " for student with id: " + appUserDetails.getId());
        }

        var project = projectService.getByProjectId(projectId);

        return ResponseEntity.ok(ProjectResponseDto.fromEntity(project));
    }

    @GetMapping("/{projectId}/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<ProjectResponseDto> getStudentProjectForTeacher(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Fetching project, project id: {}", projectId);
        var project = projectService.getByProjectId(projectId);

        return ResponseEntity.ok(ProjectResponseDto.fromEntity(project, true));
    }

    @GetMapping("/work/{workId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByWork(
            @PathVariable Long workId,
            @AuthenticationPrincipal UserDetails userDetails) {

        AppUserDetails appUserDetails = (AppUserDetails) userDetails;
        log.info("Fetching projects by work id: {}", workId);

        List<Project> projects = (appUserDetails.getRole() == UserRole.STUDENT)
                ? projectService.getAllByWorkIdAndStudentId(workId, appUserDetails.getId())
                : projectService.getAllByWorkId(workId);

        List<ProjectResponseDto> projectDtos = projects.stream()
                .map(ProjectResponseDto::fromEntity)
                .toList();

        return ResponseEntity.ok(projectDtos);
    }

    @GetMapping("/work/{workId}/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByWorkForTeacher(
            @PathVariable Long workId) {

        log.info("Fetching projects by work id: {} for teacher", workId);
        List<Project> projects = projectService.getAllByWorkId(workId);

        List<ProjectResponseDto> projectDtos = projects.stream()
                .map(project -> ProjectResponseDto.fromEntity(project, true))
                .toList();

        return ResponseEntity.ok(projectDtos);
    }

    @PostMapping("/{projectId}/approve")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<ProjectResponseDto> approveProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Approving project with id: {}", projectId);
        Long teacherId = ((AppUserDetails) userDetails).getId();

        if (!projectService.isProjectBelongsToTeacher(projectId, teacherId)) {
            log.error("Project with id: {} does not belong to teacher with id: {}", projectId, teacherId);
            throw new IllegalArgumentException(
                    "Project not found with id: " + projectId + " for teacher with id: " + teacherId);
        }

        var updatedProject = projectService.approve(projectId);

        return ResponseEntity.ok(ProjectResponseDto.fromEntityBasic(updatedProject));
    }

}
