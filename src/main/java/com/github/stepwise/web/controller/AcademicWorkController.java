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
import com.github.stepwise.entity.AcademicWorkChapter;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.AcademicWorkService;
import com.github.stepwise.web.dto.CreateWorkDto;
import com.github.stepwise.web.dto.WorkChapterDto;
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
  public ResponseEntity<Void> createWork(@Valid @RequestBody CreateWorkDto workDto) {
    log.info("Creating new academic work: {}", workDto);

    var newAcademicWork = new AcademicWork(workDto.getTitle(), workDto.getDescription(),
        workDto.getType(), workDto.getChapters().size());

    List<AcademicWorkChapter> chapters =
        workDto.getChapters().stream().map(dto -> new AcademicWorkChapter(dto.getTitle(),
            dto.getIndex(), dto.getDescription(), newAcademicWork)).toList();

    academicWorkService.create(newAcademicWork, chapters, workDto.getGroupId(),
        workDto.getTeacherId());

    return new ResponseEntity<Void>(HttpStatus.CREATED);
  }

  @GetMapping("/group/{groupId}")
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<List<WorkResponseDto>> getWorksByGroupId(@PathVariable Long groupId) {
    log.info("Fetching academic works for group with id: {}", groupId);

    List<AcademicWork> works = academicWorkService.getByGroupId(groupId);

    List<WorkResponseDto> worksDto = works.stream()
        .map(work -> new WorkResponseDto(work.getId(), work.getTitle(), work.getDescription(),
            work.getCountOfChapters(), work.getType(), work.getTeacher().getEmail(),
            work.getTeacher().getProfile().getFirstName(),
            work.getTeacher().getProfile().getLastName(),
            work.getTeacher().getProfile().getMiddleName(), null))
        .toList();

    return new ResponseEntity<>(worksDto, HttpStatus.OK);
  }


  @GetMapping("/student")
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<List<WorkResponseDto>> getStudentWorks(
      @RequestParam(required = false) String id, @AuthenticationPrincipal UserDetails userDetails) {
    AppUserDetails appUserDetails = (AppUserDetails) userDetails;
    Long studentIdToUse = id == null ? appUserDetails.getId() : Long.valueOf(id);

    if (appUserDetails.getRole() == UserRole.STUDENT
        && !studentIdToUse.equals(appUserDetails.getId())) {
      log.error("Student {} is tryint to access works of student with id: {}",
          appUserDetails.getId(), studentIdToUse);
      throw new IllegalArgumentException("Access denied: student with id " + appUserDetails.getId()
          + " cannot access works of student with id " + studentIdToUse);
    }

    if (appUserDetails.getRole() != UserRole.STUDENT && studentIdToUse == null) {
      log.error("studnetId is null");
      throw new IllegalArgumentException("Student ID cannot be null");
    }


    log.info("Fetching academic works for student with id: {}", studentIdToUse);

    List<AcademicWork> works = academicWorkService.getByStudentId(studentIdToUse);

    List<WorkResponseDto> worksDto = works.stream()
        .map(work -> new WorkResponseDto(work.getId(), work.getTitle(), work.getDescription(),
            work.getCountOfChapters(), work.getType(), work.getTeacher().getEmail(),
            work.getTeacher().getProfile().getFirstName(),
            work.getTeacher().getProfile().getLastName(),
            work.getTeacher().getProfile().getMiddleName(), null))
        .toList();

    return new ResponseEntity<>(worksDto, HttpStatus.OK);
  }

  @GetMapping("/{workId}")
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<WorkResponseDto> getWorkById(@PathVariable Long workId) {
    log.info("Fetching academic work with id: {}", workId);

    AcademicWork work = academicWorkService.getById(workId);

    List<WorkChapterDto> chaptersDto = work.getAcademicWorkChapters().stream()
        .map(chapter -> new WorkChapterDto(chapter.getIndexOfChapter(), chapter.getTitle(),
            chapter.getDescription()))
        .toList();

    WorkResponseDto workDto = new WorkResponseDto(work.getId(), work.getTitle(),
        work.getDescription(), work.getCountOfChapters(), work.getType(),
        work.getTeacher().getEmail(), work.getTeacher().getProfile().getFirstName(),
        work.getTeacher().getProfile().getLastName(),
        work.getTeacher().getProfile().getMiddleName(), chaptersDto);

    return new ResponseEntity<>(workDto, HttpStatus.OK);
  }

}
