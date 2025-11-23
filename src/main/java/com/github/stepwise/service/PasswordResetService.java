package com.github.stepwise.service;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.github.stepwise.entity.PasswordResetToken;
import com.github.stepwise.entity.User;
import com.github.stepwise.repository.PasswordResetTokenRepository;
import com.github.stepwise.repository.UserRepository;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${client.refresh-token-url}")
    private String clientRedirectUrl;

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Optional<PasswordResetToken> existingToken = tokenRepository.findByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        Date expiryDate = new Date(System.currentTimeMillis() + 3600000);

        PasswordResetToken resetToken = existingToken.map(t -> {
            t.setToken(token);
            t.setExpiryDate(expiryDate);
            return t;
        }).orElseGet(() -> new PasswordResetToken(token, user, expiryDate));

        tokenRepository.save(resetToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setFrom(fromEmail);
        message.setSubject("Password Reset Request");

        message.setText(String.format(
                "Ваш код для сброса парля: %s\nЕсли вы пользуетесь web-приложением перейдите по ссылке: %s",
                token, clientRedirectUrl + token));
        mailSender.send(message);

        log.info("Password reset link sent to {}", email);
    }

    public void resetPassword(
            String token,
            @NotBlank(message = "password must not be blank") @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$", message = "Password is not valid: password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character") String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiryDate().before(new Date()))
            throw new RuntimeException("Token expired");

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        if (user.getIsTempPassword()) {
            user.setTempPassword(null);
            user.setIsTempPassword(false);
        }

        userRepository.save(user);

        tokenRepository.delete(resetToken);

        log.info("Password reset successful for user: {}", user.getUsername());
    }

}
