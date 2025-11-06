package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.stepwise.entity.PasswordResetToken;
import com.github.stepwise.entity.User;
import com.github.stepwise.repository.PasswordResetTokenRepository;
import com.github.stepwise.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User user;
    private PasswordResetToken resetToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("oldpassword")
                .isTempPassword(true)
                .tempPassword("temppass")
                .build();

        resetToken = new PasswordResetToken("test-token", user,
                new Date(System.currentTimeMillis() + 3600000));
    }

    @Test
    void requestPasswordReset_WhenUserExists_ShouldSendResetEmail() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(resetToken);

        passwordResetService.requestPasswordReset(email);

        verify(userRepository, times(1)).findByEmail(email);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void requestPasswordReset_WhenUserNotExists_ShouldThrowException() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.requestPasswordReset(email));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void resetPassword_WithValidToken_ShouldResetPassword() {
        String token = "valid-token";
        String newPassword = "newpassword123";
        String encodedPassword = "encoded-newpassword123";

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        passwordResetService.resetPassword(token, newPassword);

        verify(tokenRepository, times(1)).findByToken(token);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).delete(resetToken);

        assertEquals(encodedPassword, user.getPassword());
        assertFalse(user.getIsTempPassword());
        assertNull(user.getTempPassword());
    }

    @Test
    void resetPassword_WithInvalidToken_ShouldThrowException() {
        String token = "invalid-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(token, "newpassword"));

        assertEquals("Invalid token", exception.getMessage());
        verify(tokenRepository, times(1)).findByToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).delete(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_WithExpiredToken_ShouldThrowException() {
        String token = "expired-token";
        PasswordResetToken expiredToken = new PasswordResetToken(token, user,
                new Date(System.currentTimeMillis() - 3600000)); // 1 hour ago

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> passwordResetService.resetPassword(token, "newpassword"));

        assertEquals("Token expired", exception.getMessage());
        verify(tokenRepository, times(1)).findByToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).delete(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_WhenUserHasTempPassword_ShouldClearTempPassword() {
        String token = "valid-token";
        String newPassword = "newpassword123";
        String encodedPassword = "encoded-newpassword123";

        User userWithTempPassword = User.builder()
                .id(2L)
                .username("tempuser")
                .email("temp@example.com")
                .password("oldpassword")
                .isTempPassword(true)
                .tempPassword("oldtemppassword")
                .build();

        PasswordResetToken tokenWithTempUser = new PasswordResetToken(token, userWithTempPassword,
                new Date(System.currentTimeMillis() + 3600000));

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenWithTempUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(userWithTempPassword);

        passwordResetService.resetPassword(token, newPassword);

        verify(userRepository, times(1)).save(userWithTempPassword);
        assertEquals(encodedPassword, userWithTempPassword.getPassword());
        assertFalse(userWithTempPassword.getIsTempPassword());
        assertNull(userWithTempPassword.getTempPassword());
    }

    @Test
    void resetPassword_WhenUserHasNoTempPassword_ShouldNotAffectTempPasswordFields() {
        String token = "valid-token";
        String newPassword = "newpassword123";
        String encodedPassword = "encoded-newpassword123";

        User userWithoutTempPassword = User.builder()
                .id(3L)
                .username("normaluser")
                .email("normal@example.com")
                .password("oldpassword")
                .isTempPassword(false)
                .tempPassword(null)
                .build();

        PasswordResetToken tokenWithNormalUser = new PasswordResetToken(token, userWithoutTempPassword,
                new Date(System.currentTimeMillis() + 3600000));

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(tokenWithNormalUser));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(userWithoutTempPassword);

        passwordResetService.resetPassword(token, newPassword);

        verify(userRepository, times(1)).save(userWithoutTempPassword);
        assertEquals(encodedPassword, userWithoutTempPassword.getPassword());
        assertFalse(userWithoutTempPassword.getIsTempPassword());
        assertNull(userWithoutTempPassword.getTempPassword());
    }

    @Test
    void requestPasswordReset_ShouldCreateTokenWithCorrectExpiry() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> {
            PasswordResetToken savedToken = invocation.getArgument(0);
            assertNotNull(savedToken.getToken());
            assertNotNull(savedToken.getExpiryDate());
            assertTrue(savedToken.getExpiryDate().after(new Date()));
            return savedToken;
        });

        passwordResetService.requestPasswordReset(email);

        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    }

}
