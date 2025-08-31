package com.github.stepwise.web.controller;

import java.util.List;
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
public class UserContoller {

  private final UserService userService;

  @GetMapping("/student")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<PageResponse<UserResponseDto>> getAllStudents(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) {
    log.info("Fetching all users");

    var users = userService.getAllStudents(PageRequest.of(pageNumber, pageSize));

    var content = users.getContent().stream()
        .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), null,
            user.getProfile().getFirstName(), user.getProfile().getLastName(),
            user.getProfile().getMiddleName(), user.getProfile().getPhoneNumber(),
            user.getProfile().getAddress()))
        .toList();

    return new ResponseEntity<>(new PageResponse<UserResponseDto>(content, users.getTotalPages()),
        HttpStatus.OK);
  }

  @GetMapping("/student/{groupId}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<List<UserResponseDto>> getAllStudentsByGroupId(@PathVariable Long groupId) {
    log.info("Fetching users by groupId: {}", groupId);

    if (groupId == null) {
      log.error("Group ID is null");
      throw new IllegalArgumentException("Group ID id null");
    }

    List<User> users = userService.getAllStudentsByGroupId(groupId);

    List<UserResponseDto> usersDto = users.stream()
        .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(),
            user.getUsername(), user.getProfile().getFirstName(), user.getProfile().getLastName(),
            user.getProfile().getMiddleName(), user.getProfile().getPhoneNumber(),
            user.getProfile().getAddress()))
        .toList();

    return new ResponseEntity<>(usersDto, HttpStatus.OK);
  }

  @PutMapping("/profile")
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<UserResponseDto> updateProfile(@Valid @RequestBody ProfileDto profileDto,
      @AuthenticationPrincipal UserDetails userDetails) {
    log.info("Updating user profile: {}", profileDto);

    AppUserDetails appUserDetails = (AppUserDetails) userDetails;
    Long userIdForUpdate = appUserDetails.getId();

    if (appUserDetails.getRole() == UserRole.ADMIN) {
      if (profileDto.getId() == null)
        throw new IllegalArgumentException("Profile ID must not be null for student or teacher");

      userIdForUpdate = profileDto.getId();
    }

    User user = userService.updateProfile(userIdForUpdate, profileDto.getFirstName(),
        profileDto.getLastName(), profileDto.getMiddleName(), profileDto.getPhoneNumber(),
        profileDto.getAddress());


    UserResponseDto userDto = new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(),
        user.getUsername(), user.getProfile().getFirstName(), user.getProfile().getLastName(),
        user.getProfile().getMiddleName(), user.getProfile().getPhoneNumber(),
        user.getProfile().getAddress());

    return new ResponseEntity<>(userDto, HttpStatus.OK);
  }

  @GetMapping("/profile/my")
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<UserResponseDto> getMyProfile(
      @AuthenticationPrincipal UserDetails userDetails) {
    AppUserDetails appUserDetails = (AppUserDetails) userDetails;

    log.info("Fetching my profile, userId: {}", appUserDetails.getId());

    User u = userService.findById(appUserDetails.getId());

    UserResponseDto userDto = new UserResponseDto(u.getId(), u.getUsername(), u.getEmail(),
        u.getProfile().getFirstName(), u.getProfile().getLastName(), u.getProfile().getMiddleName(),
        u.getProfile().getPhoneNumber(), u.getProfile().getAddress());

    return new ResponseEntity<>(userDto, HttpStatus.OK);
  }

}
