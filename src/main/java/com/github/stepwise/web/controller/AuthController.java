package com.github.stepwise.web.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.github.stepwise.entity.User;
import com.github.stepwise.service.AuthService;
import com.github.stepwise.service.PasswordResetService;
import com.github.stepwise.web.dto.MessageResponse;
import com.github.stepwise.web.dto.ResetPasswrodDto;
import com.github.stepwise.web.dto.SignInDto;
import com.github.stepwise.web.dto.SignInResponseDto;
import com.github.stepwise.web.dto.SignUpDto;
import com.github.stepwise.web.dto.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    private final PasswordResetService passwordResetService;

    @PostMapping("/signin")
    public SignInResponseDto authenticateUser(@Valid @RequestBody SignInDto signInDto) {
        log.info("Authenticating user: {}", signInDto.getUsername());

        User user = authService.getUserByPrincipals(signInDto.getUsername(), signInDto.getPassword());
        String token = authService.getTokenByUsername(user.getUsername());

        return new SignInResponseDto(new UserResponseDto(user.getId(), user.getRole().name()), token,
                user.getIsTempPassword());
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> registerUser(@Valid @RequestBody SignUpDto userDto) {
        log.info("Registering user: {}", userDto.getUsername());

        if (authService.isUsernameTaken(userDto.getUsername())) {
            log.warn("username {} is already taken", userDto.getUsername());
            return new ResponseEntity<>(new MessageResponse("Error: Username is already taken"),
                    HttpStatus.CONFLICT);
        }

        User registeredUser = authService.registerUser(new User(userDto.getUsername(),
                userDto.getPassword(), userDto.getEmail(), userDto.getRole()));

        UserResponseDto resDto = new UserResponseDto(registeredUser.getId());

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    @PostMapping("/password/reset-request")
    public ResponseEntity<String> requestReset(@RequestParam String email) {
        log.info("Password reset requested for email: {}", email);
        passwordResetService.requestPasswordReset(email);

        return new ResponseEntity<>("Reset link sent to email", HttpStatus.OK);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<String> reset(@RequestBody @Valid ResetPasswrodDto resetPasswrodDto) {
        log.info("Resetting password with token: {}", resetPasswrodDto.getToken());
        passwordResetService.resetPassword(resetPasswrodDto.getToken(),
                resetPasswrodDto.getNewPassword());

        return new ResponseEntity<>("Password reset successful", HttpStatus.OK);
    }

}
