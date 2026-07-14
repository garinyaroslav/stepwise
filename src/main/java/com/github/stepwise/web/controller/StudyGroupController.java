package com.github.stepwise.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.service.StudyGroupService;
import com.github.stepwise.web.dto.CreateGroupDto;
import com.github.stepwise.web.dto.GroupResponseDto;
import com.github.stepwise.web.dto.StudyGroupResponseDto;
import com.github.stepwise.web.dto.UpdateGroupDto;
import com.github.stepwise.web.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> createGroup(@Valid @RequestBody CreateGroupDto groupDto) {
        studyGroupService.create(groupDto.getName(), groupDto.getStudentIds());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public List<GroupResponseDto> getAllGroups(@RequestParam(required = false) String search) {
        return studyGroupService.findAllSummaries(search);
    }

    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public StudyGroupResponseDto getGroup(@PathVariable Long groupId) {
        StudyGroup group = studyGroupService.findById(groupId);
        return toResponseDto(group);
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public StudyGroupResponseDto updateGroup(@Valid @RequestBody UpdateGroupDto groupDto) {
        StudyGroup updated = studyGroupService.update(groupDto.getId(), groupDto.getStudentIds());
        return toResponseDto(updated);
    }

    private StudyGroupResponseDto toResponseDto(StudyGroup group) {
        List<UserResponseDto> students = group.getStudents().stream()
                .map(UserResponseDto::fromUser)
                .toList();
        return new StudyGroupResponseDto(group.getId(), group.getName(), students);
    }

}
