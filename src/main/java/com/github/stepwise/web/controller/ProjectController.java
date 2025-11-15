package com.github.stepwise.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
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
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.ProjectService;
import com.github.stepwise.web.dto.ExplanatoryNoteItemResponseDto;
import com.github.stepwise.web.dto.ProjectResponseDto;
import com.github.stepwise.web.dto.UpdateProjectDto;
import com.github.stepwise.web.dto.UserResponseDto;

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

        Project updatedProject = projectService.updateProject(
                new Project(projectDto.getId(), projectDto.getTitle(), projectDto.getDescription()));

        return new ResponseEntity<UpdateProjectDto>(new UpdateProjectDto(updatedProject.getId(),
                updatedProject.getTitle(), updatedProject.getDescription()), HttpStatus.OK);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ProjectResponseDto> getStudentProject(@PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Fetching project, project id: {}", projectId);

        AppUserDetails appUserDetails = (AppUserDetails) userDetails;

        if (appUserDetails.getRole() == UserRole.STUDENT) {
            if (projectService.isProjectBelongsToStudent(projectId, appUserDetails.getId()))
                log.info("Project with id: {} belongs to student with id: {}", projectId,
                        appUserDetails.getId());
            else {
                log.error("Project with id: {} does not belong to student with id: {}", projectId,
                        appUserDetails.getId());
                throw new IllegalArgumentException("Project not found with id: " + projectId
                        + " for student with id: " + appUserDetails.getId());
            }

        }

        Project project = projectService.getByProjectId(projectId);

        User student = project.getStudent();

        UserResponseDto owner = new UserResponseDto(student.getId(), student.getUsername(),
                student.getEmail(), student.getProfile().getFirstName(), student.getProfile().getLastName(),
                student.getProfile().getMiddleName());

        List<ExplanatoryNoteItemResponseDto> items = project.getItems().stream()
                .map(item -> new ExplanatoryNoteItemResponseDto(item.getId(), item.getOrderNumber(),
                        item.getStatus(), item.getFileName(), item.getTeacherComment(), item.getDraftedAt(),
                        item.getSubmittedAt(), item.getApprovedAt(), item.getRejectedAt()))
                .toList();

        ProjectResponseDto projectDto = new ProjectResponseDto(project.getId(), project.getTitle(),
                project.getDescription(), owner, items, project.isApprovedForDefense());

        return new ResponseEntity<>(projectDto, HttpStatus.OK);
    }

    @GetMapping("/work/{workId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByWork(@PathVariable Long workId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUserDetails appUserDetails = (AppUserDetails) userDetails;

        log.info("Fetching projects by work id: {}", workId);

        List<Project> projects;

        if (appUserDetails.getRole() == UserRole.STUDENT)
            projects = projectService.getAllByWorkIdAndStudentId(workId, appUserDetails.getId());
        else
            projects = projectService.getAllByWorkId(workId);

        List<ProjectResponseDto> projectDtos = new ArrayList<>(35);

        for (Project project : projects) {
            User student = project.getStudent();

            UserResponseDto owner = new UserResponseDto(student.getId(), student.getUsername(),
                    student.getEmail(), student.getProfile().getFirstName(),
                    student.getProfile().getLastName(), student.getProfile().getMiddleName());

            List<ExplanatoryNoteItemResponseDto> items = project.getItems().stream()
                    .map(item -> new ExplanatoryNoteItemResponseDto(item.getId(), item.getOrderNumber(),
                            item.getStatus(), item.getFileName(), item.getTeacherComment(), item.getDraftedAt(),
                            item.getSubmittedAt(), item.getApprovedAt(), item.getRejectedAt()))
                    .toList();

            ProjectResponseDto projectDto = new ProjectResponseDto(project.getId(), project.getTitle(),
                    project.getDescription(), owner, items, project.isApprovedForDefense());

            projectDtos.add(projectDto);
        }

        return new ResponseEntity<>(projectDtos, HttpStatus.OK);
    }

    @GetMapping("/work/{workId}/teacher")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<List<ProjectResponseDto>> getProjectsByWorkForTeacher(
            @PathVariable Long workId) {
        log.info("Fetching projects by work id: {} for teacher", workId);

        List<Project> projects = projectService.getAllByWorkId(workId);

        List<ProjectResponseDto> projectDtos = new ArrayList<>(35);

        for (Project project : projects) {
            User student = project.getStudent();

            UserResponseDto owner = new UserResponseDto(student.getId(), student.getUsername(),
                    student.getEmail(), student.getProfile().getFirstName(),
                    student.getProfile().getLastName(), student.getProfile().getMiddleName());

            List<ExplanatoryNoteItemResponseDto> items = project.getItems().stream()
                    .filter(item -> item.getStatus() != ItemStatus.DRAFT)
                    .map(item -> new ExplanatoryNoteItemResponseDto(item.getId(), item.getOrderNumber(),
                            item.getStatus(), item.getFileName(), item.getTeacherComment(), item.getDraftedAt(),
                            item.getSubmittedAt(), item.getApprovedAt(), item.getRejectedAt()))
                    .toList();

            ProjectResponseDto projectDto = new ProjectResponseDto(project.getId(), project.getTitle(),
                    project.getDescription(), owner, items, project.isApprovedForDefense());

            projectDtos.add(projectDto);
        }

        return new ResponseEntity<>(projectDtos, HttpStatus.OK);
    }

    @PostMapping("/{projectId}/approve")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<ProjectResponseDto> approveProject(@PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Approving project with id: {}", projectId);
        Long teacherId = ((AppUserDetails) userDetails).getId();

        if (!projectService.isProjectBelongsToTeacher(projectId, teacherId)) {
            log.error("Project with id: {} does not belong to teacher with id: {}", projectId, teacherId);
            throw new IllegalArgumentException(
                    "Project not found with id: " + projectId + " for teacher with id: " + teacherId);
        }

        Project updatedProject = projectService.approve(projectId);

        ProjectResponseDto projectDto = new ProjectResponseDto(updatedProject.getId(), updatedProject.getTitle(),
                updatedProject.getDescription(), null, null, updatedProject.isApprovedForDefense());

        return new ResponseEntity<>(projectDto, HttpStatus.OK);

    }

}
