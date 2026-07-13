package com.github.stepwise.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.entity.User;
import com.github.stepwise.service.AuthService;
import com.github.stepwise.service.PasswordResetService;
import com.github.stepwise.web.dto.MessageResponse;
import com.github.stepwise.web.dto.ResetPasswordDto;
import com.github.stepwise.web.dto.SignInDto;
import com.github.stepwise.web.dto.SignInResponseDto;
import com.github.stepwise.web.dto.SignUpDto;
import com.github.stepwise.web.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final PasswordResetService passwordResetService;

    @PostMapping("/sessions")
    public SignInResponseDto authenticateUser(@Valid @RequestBody SignInDto signInDto) {
        return authService.authenticate(signInDto);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody SignUpDto userDto) {
        User registeredUser = authService.registerUser(userDto);
        UserResponseDto response = UserResponseDto.builder().id(registeredUser.getId()).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/password-reset-requests")
    public ResponseEntity<MessageResponse> requestReset(@RequestParam String email) {
        passwordResetService.requestPasswordReset(email);

        return ResponseEntity.ok(new MessageResponse("Reset link sent to email"));
    }

    @PatchMapping("/passwords")
    public ResponseEntity<MessageResponse> reset(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        passwordResetService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());

        return ResponseEntity.ok(new MessageResponse("Password reset successful"));
    }

}
