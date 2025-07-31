package com.github.stepwise.web.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.AcademicWorkChapter;
import com.github.stepwise.service.AcademicWorkService;
import com.github.stepwise.web.dto.CreateWorkDto;
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

}
