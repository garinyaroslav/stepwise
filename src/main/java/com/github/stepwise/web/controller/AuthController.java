package com.github.stepwise.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.stepwise.entity.User;
import com.github.stepwise.service.AuthService;
import com.github.stepwise.web.dto.MessageResponse;
import com.github.stepwise.web.dto.SignInDto;
import com.github.stepwise.web.dto.SignUpDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final AuthService authService;

  @PostMapping("/signin")
  public String authenticateUser(@Valid @RequestBody SignInDto user) {
    log.info("Authenticating user: {}", user.getUsername());

    return authService.getTokenByPrincipals(new User(user.getUsername(), user.getPassword()));
  }

  @PostMapping("/signup")
  public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody SignUpDto userDto) {
    log.info("Registering user: {}", userDto.getUsername());

    if (authService.isUsernameTaken(userDto.getUsername())) {
      log.warn("username {} is already taken", userDto.getUsername());
      return new ResponseEntity<MessageResponse>(
          new MessageResponse("Error: Username is already taken"), HttpStatus.CONFLICT);
    }

    authService.registerUser(new User(userDto.getUsername(), userDto.getPassword(),
        userDto.getEmail(), userDto.getRole()));

    return new ResponseEntity<MessageResponse>(new MessageResponse("User registered successfully"),
        HttpStatus.CREATED);
  }
}
