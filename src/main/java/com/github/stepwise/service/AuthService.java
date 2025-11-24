package com.github.stepwise.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.utils.JwtUtil;
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

    public boolean isUsernameTaken(String username) {
        log.info("Checking if username {} exists", username);
        boolean exists = userRepository.existsByUsername(username);

        return exists;
    }

    public User registerUser(User user) {
        if (user.getRole() == null)
            user.setRole(UserRole.STUDENT);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public User getUserByPrincipals(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();

        return userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String getTokenByUsername(String username) {
        return jwtUtils.generateToken(username);
    }

}
