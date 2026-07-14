package com.github.stepwise.web.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import com.github.stepwise.web.dto.DefenseDto.DefendProjectDto;
import com.github.stepwise.web.dto.ProjectResponseDto;
import com.github.stepwise.web.dto.UpdateProjectDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public UpdateProjectDto updateProject(@Valid @RequestBody UpdateProjectDto projectDto) {
        Project updatedProject = projectService.updateProject(projectDto.toEntity());
        return UpdateProjectDto.fromEntity(updatedProject);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ProjectResponseDto getStudentProject(@PathVariable Long projectId,
            @AuthenticationPrincipal AppUserDetails principal) {
        Project project = principal.getRole() == UserRole.STUDENT
                ? projectService.getForStudent(projectId, principal.getId())
                : projectService.getByProjectId(projectId);
        return ProjectResponseDto.fromEntity(project);
    }

    @GetMapping("/{projectId}/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ProjectResponseDto getStudentProjectForTeacher(@PathVariable Long projectId) {
        Project project = projectService.getByProjectId(projectId);
        return ProjectResponseDto.fromEntity(project, true);
    }

    @GetMapping("/work/{workId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    public List<ProjectResponseDto> getProjectsByWork(@PathVariable Long workId,
            @AuthenticationPrincipal AppUserDetails principal) {
        List<Project> projects = projectService.getForRequester(workId, principal.getId(), principal.getRole());
        return projects.stream().map(ProjectResponseDto::fromEntity).toList();
    }

    @GetMapping("/work/{workId}/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public List<ProjectResponseDto> getProjectsByWorkForTeacher(@PathVariable Long workId) {
        return projectService.getAllByWorkId(workId).stream()
                .map(project -> ProjectResponseDto.fromEntity(project, true))
                .toList();
    }

    @PostMapping("/{projectId}/approval")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ProjectResponseDto approveProject(@PathVariable Long projectId,
            @AuthenticationPrincipal AppUserDetails principal) {
        Project updatedProject = projectService.approveAsTeacher(projectId, principal.getId());
        return ProjectResponseDto.fromEntityBasic(updatedProject);
    }

    @PostMapping("/{projectId}/defend")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ProjectResponseDto defendProject(@PathVariable Long projectId,
            @Valid @RequestBody DefendProjectDto dto,
            @AuthenticationPrincipal AppUserDetails principal) {
        Project updatedProject = projectService.defendAsTeacher(projectId, principal.getId(), dto.getGrade());
        return ProjectResponseDto.fromEntityBasic(updatedProject);
    }

}
