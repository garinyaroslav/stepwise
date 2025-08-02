package com.github.stepwise.web.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.service.StudyGroupService;
import com.github.stepwise.web.dto.CreateGroupDto;
import com.github.stepwise.web.dto.UpdateGroupDto;
import com.github.stepwise.web.dto.StudyGroupResponseDto;
import com.github.stepwise.web.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
@Slf4j
public class StudyGroupController {

  private final StudyGroupService studyGroupService;

  @PostMapping
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<Void> createGroup(@Valid @RequestBody CreateGroupDto groupDto) {
    log.info("Creating group with name: {}", groupDto.getName());

    studyGroupService.create(groupDto.getName(), groupDto.getStudentIds());

    return new ResponseEntity<Void>(HttpStatus.CREATED);
  }

  @PutMapping
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<StudyGroupResponseDto> updateGroup(
      @Valid @RequestBody UpdateGroupDto groupDto) {
    log.info("Updating group with id: {}", groupDto.getId());

    StudyGroup updatedStudyGroup =
        studyGroupService.update(groupDto.getId(), groupDto.getStudentIds());

    StudyGroupResponseDto updatedStudyGroupDto = new StudyGroupResponseDto(
        updatedStudyGroup.getName(), updatedStudyGroup.getStudents().stream()
            .map(user -> new UserResponseDto(user.getUsername(), user.getEmail())).toList());


    return new ResponseEntity<StudyGroupResponseDto>(updatedStudyGroupDto, HttpStatus.OK);
  }

}
