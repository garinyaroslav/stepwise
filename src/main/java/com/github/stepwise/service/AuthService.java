package com.github.stepwise.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.utils.JwtUtil;
import com.github.stepwise.web.dto.SignInDto;
import com.github.stepwise.web.dto.SignInResponseDto;
import com.github.stepwise.web.dto.SignUpDto;
import com.github.stepwise.web.dto.UserResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtils;

    public SignInResponseDto authenticate(SignInDto signInDto) {
        log.info("Authenticating user: {}", signInDto.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signInDto.getUsername(), signInDto.getPassword()));
        AppUserDetails principal = (AppUserDetails) authentication.getPrincipal();

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + principal.getId()));

        String token = jwtUtils.generateToken(user.getUsername());

        return new SignInResponseDto(
                UserResponseDto.fromIdAndRole(user.getId(), user.getRole().name()),
                token,
                user.getTempPassword() != null);
    }

    @Transactional
    public User registerUser(SignUpDto userDto) {
        log.info("Registering user: {}", userDto.getUsername());

        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new IllegalArgumentException("Username is already taken: " + userDto.getUsername());
        }

        UserRole role = userDto.getRole() != null ? userDto.getRole() : UserRole.STUDENT;
        User user = new User(userDto.getUsername(), userDto.getPassword(), userDto.getEmail(), role);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        return userRepository.save(user);
    }

}
