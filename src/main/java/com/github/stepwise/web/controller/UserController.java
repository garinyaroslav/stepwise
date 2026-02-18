package com.github.stepwise.web.controller;

import java.io.IOException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.ExcelExportService;
import com.github.stepwise.service.UserService;
import com.github.stepwise.web.dto.PageResponse;
import com.github.stepwise.web.dto.ProfileDto;
import com.github.stepwise.web.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    private final ExcelExportService excelExportService;

    @GetMapping("/student")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<PageResponse<UserResponseDto>> getAllStudents(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {

        log.info("Fetching all students");
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        Page<User> users = (search != null && !search.isBlank())
                ? userService.getAllStudents(search, pageRequest)
                : userService.getAllStudents(pageRequest);

        List<UserResponseDto> content = users.stream()
                .map(UserResponseDto::fromUser)
                .toList();

        return ResponseEntity.ok(new PageResponse<>(content, users.getTotalPages()));
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<PageResponse<UserResponseDto>> getAllTeachers(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {

        log.info("Fetching all teachers");
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        Page<User> users = (search != null && !search.isBlank())
                ? userService.getAllTeachers(search, pageRequest)
                : userService.getAllTeachers(pageRequest);

        List<UserResponseDto> content = users.stream()
                .map(UserResponseDto::fromUser)
                .toList();

        return ResponseEntity.ok(new PageResponse<>(content, users.getTotalPages()));
    }

    @GetMapping("/student/{groupId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<List<UserResponseDto>> getAllStudentsByGroupId(@PathVariable Long groupId) {
        log.info("Fetching users by groupId: {}", groupId);
        validateGroupId(groupId);

        List<User> users = userService.getAllStudentsByGroupId(groupId);
        List<UserResponseDto> usersDto = users.stream()
                .map(UserResponseDto::fromUserWithFullInfo)
                .toList();

        return ResponseEntity.ok(usersDto);
    }

    @GetMapping("/student/{groupId}/exportation")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<byte[]> exportUsersWithTempPasswords(@PathVariable Long groupId) {
        log.info("Exporting users with temporary passwords for groupId: {}", groupId);
        validateGroupId(groupId);

        try {
            List<User> users = userService.getUsersWithTempPasswordByGroupId(groupId);
            byte[] excelData = excelExportService.exportUsersWithTempPasswords(users);

            String filename = String.format("users_passwords_%d.xlsx", groupId);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                    .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .body(excelData);

        } catch (IOException e) {
            log.error("Error generating Excel file for groupId: {}", groupId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<UserResponseDto> updateProfile(
            @Valid @RequestBody ProfileDto profileDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Updating user profile: {}", profileDto);

        AppUserDetails appUserDetails = (AppUserDetails) userDetails;

        Long userIdForUpdate = getUserIdForUpdate(profileDto, appUserDetails);
        User updatedUser = userService.updateProfile(userIdForUpdate, profileDto.getFirstName(),
                profileDto.getLastName(), profileDto.getMiddleName(), profileDto.getPhoneNumber(),
                profileDto.getAddress());

        return ResponseEntity.ok(UserResponseDto.fromUserWithFullInfo(updatedUser));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<UserResponseDto> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUserDetails appUserDetails = (AppUserDetails) userDetails;
        log.info("Fetching my profile, userId: {}", appUserDetails.getId());

        User user = userService.findById(appUserDetails.getId());
        return ResponseEntity.ok(UserResponseDto.fromUserWithFullInfo(user));
    }

    private void validateGroupId(Long groupId) {
        if (groupId == null) {
            log.error("Group ID is null");
            throw new IllegalArgumentException("Group ID must not be null");
        }
    }

    private Long getUserIdForUpdate(ProfileDto profileDto, AppUserDetails userDetails) {
        if (userDetails.getRole() == UserRole.ADMIN && profileDto.getId() != null) {
            return profileDto.getId();
        }

        if (userDetails.getRole() == UserRole.ADMIN && profileDto.getId() == null) {
            throw new IllegalArgumentException("Profile ID must not be null for admin");
        }

        return userDetails.getId();
    }
}
