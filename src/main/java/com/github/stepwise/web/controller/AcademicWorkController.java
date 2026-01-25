package com.github.stepwise.web.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.AcademicWorkService;
import com.github.stepwise.web.dto.CreateWorkDto;
import com.github.stepwise.web.dto.WorkResponseDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/work")
@RequiredArgsConstructor
@Slf4j
public class AcademicWorkController {

    private final AcademicWorkService academicWorkService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<Void> createWork(@Valid @RequestBody CreateWorkDto workDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Creating new academic work: {}", workDto);

        academicWorkService.create(workDto.getWorkTemplateId(), workDto.getGroupId());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<List<WorkResponseDto>> getWorksByGroupId(@PathVariable Long groupId) {
        log.info("Fetching academic works for group with id: {}", groupId);

        List<AcademicWork> works = academicWorkService.getByGroupId(groupId);
        List<WorkResponseDto> worksDto = works.stream()
                .map(WorkResponseDto::fromEntity)
                .toList();

        return ResponseEntity.ok(worksDto);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<List<WorkResponseDto>> getWorksByTeacherId(@PathVariable Long teacherId,
            @RequestParam(required = false) Long groupId) {
        log.info("Fetching academic work for teacher with id: {}, and group with id: {}", teacherId, groupId);

        List<AcademicWork> works = academicWorkService.getByTeacherAndGroupId(teacherId, groupId);
        List<WorkResponseDto> worksDto = works.stream()
                .map(WorkResponseDto::fromEntity)
                .toList();

        return ResponseEntity.ok(worksDto);
    }

    @GetMapping("/student")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<List<WorkResponseDto>> getStudentWorks(
            @RequestParam(required = false) String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUserDetails appUserDetails = (AppUserDetails) userDetails;
        Long studentIdToUse = id == null ? appUserDetails.getId() : Long.valueOf(id);

        if (appUserDetails.getRole() == UserRole.STUDENT
                && !studentIdToUse.equals(appUserDetails.getId())) {
            log.error("Student {} is trying to access works of student with id: {}",
                    appUserDetails.getId(), studentIdToUse);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (appUserDetails.getRole() != UserRole.STUDENT && studentIdToUse == null) {
            log.error("studentId is null");
            throw new IllegalArgumentException("Student ID cannot be null");
        }

        log.info("Fetching academic works for student with id: {}", studentIdToUse);

        List<AcademicWork> works = academicWorkService.getByStudentId(studentIdToUse);
        List<WorkResponseDto> worksDto = works.stream()
                .map(WorkResponseDto::fromEntity)
                .toList();

        return ResponseEntity.ok(worksDto);
    }

    @GetMapping("/{workId}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<WorkResponseDto> getWorkById(@PathVariable Long workId) {
        log.info("Fetching academic work with id: {}", workId);

        AcademicWork work = academicWorkService.getById(workId);
        WorkResponseDto workDto = WorkResponseDto.fromEntityWithChapters(work);

        return ResponseEntity.ok(workDto);
    }
}
