package com.github.stepwise.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.AcademicWorkService;
import com.github.stepwise.web.dto.CreateWorkDto;
import com.github.stepwise.web.dto.WorkResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/work")
@RequiredArgsConstructor
public class AcademicWorkController {

    private final AcademicWorkService academicWorkService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<Void> createWork(@Valid @RequestBody CreateWorkDto workDto) {
        academicWorkService.create(workDto.getWorkTemplateId(), workDto.getGroupId(), workDto.getDeadlines());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public List<WorkResponseDto> getWorksByGroupId(@PathVariable Long groupId) {
        return toDtoList(academicWorkService.getByGroupId(groupId));
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public List<WorkResponseDto> getWorksByTeacherId(@PathVariable Long teacherId,
            @RequestParam(required = false) Long groupId) {
        return toDtoList(academicWorkService.getByTeacherAndGroupId(teacherId, groupId));
    }

    @GetMapping("/student")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public List<WorkResponseDto> getStudentWorks(
            @RequestParam(required = false) String id,
            @AuthenticationPrincipal AppUserDetails principal) {
        List<AcademicWork> works = academicWorkService.getWorksForRequester(id, principal.getId(), principal.getRole());
        return toDtoList(works);
    }

    @GetMapping("/{workId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public WorkResponseDto getWorkById(@PathVariable Long workId) {
        AcademicWork work = academicWorkService.getById(workId);
        return WorkResponseDto.fromEntityWithChapters(work);
    }

    private List<WorkResponseDto> toDtoList(List<AcademicWork> works) {
        return works.stream().map(WorkResponseDto::fromEntity).toList();
    }

}
