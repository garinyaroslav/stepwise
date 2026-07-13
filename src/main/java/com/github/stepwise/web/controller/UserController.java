package com.github.stepwise.web.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.entity.User;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.ExcelExportService;
import com.github.stepwise.service.UserService;
import com.github.stepwise.web.dto.PageResponse;
import com.github.stepwise.web.dto.ProfileDto;
import com.github.stepwise.web.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final ExcelExportService excelExportService;

    @GetMapping("/student")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public PageResponse<UserResponseDto> getAllStudents(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        Page<User> page = userService.getStudents(search, PageRequest.of(pageNumber, pageSize));
        return toPageResponse(page);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public PageResponse<UserResponseDto> getAllTeachers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        Page<User> page = userService.getTeachers(search, PageRequest.of(pageNumber, pageSize));
        return toPageResponse(page);
    }

    @GetMapping("/student/{groupId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public List<UserResponseDto> getAllStudentsByGroupId(@PathVariable Long groupId) {
        return userService.getStudentsByGroupId(groupId).stream()
                .map(UserResponseDto::fromUserWithFullInfo)
                .toList();
    }

    @GetMapping("/student/{groupId}/exportation")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportUsersWithTempPasswords(@PathVariable Long groupId)
            throws IOException {
        List<User> users = userService.getUsersWithTempPasswordByGroupId(groupId);
        byte[] excelData = excelExportService.exportUsersWithTempPasswords(users);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"users_passwords_%d.xlsx\"".formatted(groupId))
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelData);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public UserResponseDto updateProfile(
            @Valid @RequestBody ProfileDto profileDto,
            @AuthenticationPrincipal AppUserDetails principal) {
        Long targetId = userService.resolveProfileTargetId(profileDto, principal.getId(), principal.getRole());
        User updated = userService.updateProfile(targetId, profileDto);
        return UserResponseDto.fromUserWithFullInfo(updated);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public UserResponseDto getMyProfile(@AuthenticationPrincipal AppUserDetails principal) {
        return UserResponseDto.fromUserWithFullInfo(userService.findById(principal.getId()));
    }

    private PageResponse<UserResponseDto> toPageResponse(Page<User> page) {
        List<UserResponseDto> content = page.stream().map(UserResponseDto::fromUser).toList();
        return new PageResponse<>(content, page.getTotalPages());
    }

}
