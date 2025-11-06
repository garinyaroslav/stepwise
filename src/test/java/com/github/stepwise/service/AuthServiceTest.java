package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.utils.JwtUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User user;
    private AppUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .role(UserRole.STUDENT)
                .build();

        userDetails = new AppUserDetails(user);
    }

    @Test
    void isUsernameTaken_WhenUsernameExists_ShouldReturnTrue() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        boolean result = authService.isUsernameTaken("existinguser");

        assertTrue(result);
        verify(userRepository, times(1)).existsByUsername("existinguser");
    }

    @Test
    void isUsernameTaken_WhenUsernameNotExists_ShouldReturnFalse() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        boolean result = authService.isUsernameTaken("newuser");

        assertFalse(result);
        verify(userRepository, times(1)).existsByUsername("newuser");
    }

    @Test
    void registerUser_WithStudentRole_ShouldEncodePasswordAndSave() {
        User userToRegister = User.builder()
                .username("newstudent")
                .password("plainpassword")
                .role(UserRole.STUDENT)
                .build();

        when(passwordEncoder.encode("plainpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(userToRegister);

        assertNotNull(result);
        assertEquals("encodedpassword", result.getPassword());
        assertEquals(UserRole.STUDENT, result.getRole());
        verify(passwordEncoder, times(1)).encode("plainpassword");
        verify(userRepository, times(1)).save(userToRegister);
    }

    @Test
    void registerUser_WithNullRole_ShouldSetDefaultStudentRole() {
        User userToRegister = User.builder()
                .username("newuser")
                .password("password")
                .role(null)
                .build();

        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(userToRegister);

        assertNotNull(result);
        assertEquals(UserRole.STUDENT, result.getRole());
        assertEquals("encodedpassword", result.getPassword());
    }

    @Test
    void registerUser_WithTeacherRole_ShouldPreserveRole() {
        User userToRegister = User.builder()
                .username("teacher")
                .password("password")
                .role(UserRole.TEACHER)
                .build();

        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(userToRegister);

        assertNotNull(result);
        assertEquals(UserRole.TEACHER, result.getRole());
        assertEquals("encodedpassword", result.getPassword());
    }

    @Test
    void getUserAndTokenByPrincipals_WithValidCredentials_ShouldReturnTokenAndUserInfo() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateToken("testuser")).thenReturn("jwt-token");

        Map<String, Object> result = authService.getUserAndTokenByPrincipals(user);

        assertNotNull(result);
        assertEquals("jwt-token", result.get("token"));
        assertEquals("STUDENT", result.get("role"));
        assertEquals(1L, result.get("userId"));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateToken("testuser");
    }

    @Test
    void getUserAndTokenByPrincipals_WithInvalidCredentials_ShouldThrowException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.getUserAndTokenByPrincipals(user));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void getUserAndTokenByPrincipals_ShouldCreateCorrectAuthenticationToken() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateToken("testuser")).thenReturn("jwt-token");

        authService.getUserAndTokenByPrincipals(user);

        verify(authenticationManager, times(1)).authenticate(
                argThat(token -> token instanceof UsernamePasswordAuthenticationToken &&
                        "testuser".equals(((UsernamePasswordAuthenticationToken) token).getPrincipal()) &&
                        "password".equals(((UsernamePasswordAuthenticationToken) token).getCredentials())));
    }
}
