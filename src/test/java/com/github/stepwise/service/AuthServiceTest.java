package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

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
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.utils.JwtUtil;
import com.github.stepwise.web.dto.SignInDto;
import com.github.stepwise.web.dto.SignInResponseDto;
import com.github.stepwise.web.dto.SignUpDto;

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
    void authenticate_WithValidCredentials_ShouldReturnSignInResponse() {
        SignInDto signInDto = new SignInDto("testuser", "password");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken("testuser")).thenReturn("jwt-token");

        SignInResponseDto result = authService.authenticate(signInDto);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertEquals(1L, result.getUser().getId());
        assertEquals(UserRole.STUDENT.name(), result.getUser().getRole());
        assertFalse(result.isTemporaryPassword());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findById(1L);
        verify(jwtUtils).generateToken("testuser");
    }

    @Test
    void authenticate_WithTempPassword_ShouldReturnTempPasswordFlagTrue() {
        User userWithTempPassword = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .tempPassword("temp123")
                .role(UserRole.STUDENT)
                .build();
        AppUserDetails detailsWithTemp = new AppUserDetails(userWithTempPassword);

        SignInDto signInDto = new SignInDto("testuser", "password");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(detailsWithTemp);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithTempPassword));
        when(jwtUtils.generateToken("testuser")).thenReturn("jwt-token");

        SignInResponseDto result = authService.authenticate(signInDto);

        assertTrue(result.isTemporaryPassword());
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldPropagateException() {
        SignInDto signInDto = new SignInDto("testuser", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.authenticate(signInDto));

        assertEquals("Invalid credentials", exception.getMessage());
        verify(userRepository, never()).findById(anyLong());
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void authenticate_WhenUserNotFound_ShouldThrowEntityNotFoundException() {
        SignInDto signInDto = new SignInDto("testuser", "password");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> authService.authenticate(signInDto));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void authenticate_ShouldBuildAuthenticationTokenFromDto() {
        SignInDto signInDto = new SignInDto("testuser", "password");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken("testuser")).thenReturn("jwt-token");

        authService.authenticate(signInDto);

        verify(authenticationManager).authenticate(argThat(token -> token instanceof UsernamePasswordAuthenticationToken
                && "testuser".equals(token.getPrincipal())
                && "password".equals(token.getCredentials())));
    }

    @Test
    void registerUser_WithStudentRole_ShouldEncodePasswordAndSave() {
        SignUpDto dto = new SignUpDto();
        dto.setUsername("newstudent");
        dto.setPassword("plainpassword");
        dto.setEmail("student@test.com");
        dto.setRole(UserRole.STUDENT);

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(passwordEncoder.encode("plainpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(dto);

        assertNotNull(result);
        assertEquals("encodedpassword", result.getPassword());
        assertEquals(UserRole.STUDENT, result.getRole());
        assertEquals("newstudent", result.getUsername());
        verify(passwordEncoder).encode("plainpassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WithNullRole_ShouldSetDefaultStudentRole() {
        SignUpDto dto = new SignUpDto();
        dto.setUsername("newuser");
        dto.setPassword("password");
        dto.setEmail("newuser@test.com");
        dto.setRole(null);

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(dto);

        assertEquals(UserRole.STUDENT, result.getRole());
        assertEquals("encodedpassword", result.getPassword());
    }

    @Test
    void registerUser_WithTeacherRole_ShouldPreserveRole() {
        SignUpDto dto = new SignUpDto();
        dto.setUsername("teacher");
        dto.setPassword("password");
        dto.setEmail("teacher@test.com");
        dto.setRole(UserRole.TEACHER);

        when(userRepository.existsByUsername("teacher")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = authService.registerUser(dto);

        assertEquals(UserRole.TEACHER, result.getRole());
        assertEquals("encodedpassword", result.getPassword());
    }

    @Test
    void registerUser_WhenUsernameTaken_ShouldThrowException() {
        SignUpDto dto = new SignUpDto("existinguser", "password", "existing@test.com", UserRole.STUDENT);

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.registerUser(dto));

        assertEquals("Username is already taken: existinguser", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
