package com.github.stepwise.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.github.stepwise.service.AcademicWorkService;
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
  public ResponseEntity<Void> createWork(@Valid @RequestBody CreateGroupDto groupDto) {
    // log.info("Creating group with name: {}", groupDto.getName());

    // studyGroupService.create(groupDto.getName(), groupDto.getStudentIds());

    return new ResponseEntity<Void>(HttpStatus.CREATED);
  }

}
