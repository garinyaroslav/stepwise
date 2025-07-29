package com.github.stepwise.web.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.stepwise.service.UserService;
import com.github.stepwise.web.dto.PageResponse;
import com.github.stepwise.web.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserContoller {

  private final UserService userService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<PageResponse<UserResponseDto>> getAllStudents(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) {
    log.info("Fetching all users");

    var users = userService.getAllStudents(PageRequest.of(pageNumber, pageSize));

    var content = users.getContent().stream()
        .map(user -> new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), null,
            user.getProfile().getFirstName(), user.getProfile().getLastName(),
            user.getProfile().getPhoneNumber(), user.getProfile().getAddress()))
        .toList();

    return new ResponseEntity<>(new PageResponse<UserResponseDto>(content, users.getTotalPages()),
        HttpStatus.OK);
  }

}
