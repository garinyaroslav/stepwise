package com.github.stepwise.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.stepwise.entity.User;
import com.github.stepwise.service.AuthService;
import com.github.stepwise.web.dto.MessageResponse;
import com.github.stepwise.web.dto.UserDto;
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
  public String authenticateUser(@Valid @RequestBody UserDto user) {
    log.info("Authenticating user: {}", user.getUsername());

    return authService.getTokenByPrincipals(new User(user.getUsername(), user.getPassword()));
  }

  @PostMapping("/signup")
  public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody UserDto user) {
    log.info("Registering user: {}", user.getUsername());

    if (authService.isUsernameTaken(user.getUsername())) {
      log.warn("username {} is already taken", user.getUsername());
      return new ResponseEntity<MessageResponse>(
          new MessageResponse("Error: Username is already taken"), HttpStatus.CONFLICT);
    }

    authService.registerUser(new User(user.getUsername(), user.getPassword(), user.getRole()));

    return new ResponseEntity<MessageResponse>(new MessageResponse("User registered successfully"),
        HttpStatus.CREATED);
  }
}
