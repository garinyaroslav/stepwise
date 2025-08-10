package com.github.stepwise.service;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
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

    String token = UUID.randomUUID().toString();
    PasswordResetToken resetToken =
        new PasswordResetToken(token, user, new Date(System.currentTimeMillis() + 3600000));

    tokenRepository.save(resetToken);

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(email);
    message.setFrom(fromEmail);
    message.setSubject("Password Reset Request");

    message.setText("To reset your password, click the link: " + clientRedirectUrl + token);
    mailSender.send(message);

    log.info("Password reset link sent to {}", email);
  }

  public void resetPassword(String token, String newPassword) {
    PasswordResetToken resetToken = tokenRepository.findByToken(token)
        .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

    if (resetToken.getExpiryDate().before(new Date()))
      throw new RuntimeException("Token expired");


    User user = resetToken.getUser();

    user.setPassword(passwordEncoder.encode(newPassword));

    userRepository.save(user);

    tokenRepository.delete(resetToken);

    log.info("Password reset successful for user: {}", user.getUsername());
  }
}


