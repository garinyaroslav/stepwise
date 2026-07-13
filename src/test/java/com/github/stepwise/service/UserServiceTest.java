package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.stepwise.entity.Profile;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.web.dto.ProfileDto;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @InjectMocks
    private UserService userService;

    private User student;
    private User teacher;
    private Profile profile;

    @BeforeEach
    void setUp() {
        profile = Profile.builder()
                .firstName("John")
                .lastName("Doe")
                .middleName("Michael")
                .phoneNumber("123-456-7890")
                .address("123 Main St")
                .build();

        student = User.builder()
                .id(1L)
                .username("student1")
                .role(UserRole.STUDENT)
                .tempPassword("pass")
                .profile(profile)
                .build();

        teacher = User.builder()
                .id(2L)
                .username("teacher1")
                .role(UserRole.TEACHER)
                .tempPassword(null)
                .profile(profile)
                .build();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("student1", result.getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void findById_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.findById(999L));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(999L);
    }

    @Test
    void getStudents_WithoutSearch_ShouldReturnAllStudents() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> studentPage = new PageImpl<>(List.of(student), pageable, 1);
        when(userRepository.findByRole(UserRole.STUDENT, pageable)).thenReturn(studentPage);

        Page<User> result = userService.getStudents(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("student1", result.getContent().get(0).getUsername());
        verify(userRepository).findByRole(UserRole.STUDENT, pageable);
        verify(userRepository, never()).findByUsernameOrFirstNameOrLastName(any(), any(), any());
    }

    @Test
    void getStudents_WithBlankSearch_ShouldReturnAllStudents() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> studentPage = new PageImpl<>(List.of(student), pageable, 1);
        when(userRepository.findByRole(UserRole.STUDENT, pageable)).thenReturn(studentPage);

        Page<User> result = userService.getStudents("   ", pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByRole(UserRole.STUDENT, pageable);
        verify(userRepository, never()).findByUsernameOrFirstNameOrLastName(any(), any(), any());
    }

    @Test
    void getStudents_WithSearch_ShouldReturnFilteredStudents() {
        String search = "john";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> studentPage = new PageImpl<>(List.of(student), pageable, 1);
        when(userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.STUDENT, pageable))
                .thenReturn(studentPage);

        Page<User> result = userService.getStudents(search, pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByUsernameOrFirstNameOrLastName(search, UserRole.STUDENT, pageable);
        verify(userRepository, never()).findByRole(any(), any());
    }

    @Test
    void getTeachers_WithoutSearch_ShouldReturnAllTeachers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> teacherPage = new PageImpl<>(List.of(teacher), pageable, 1);
        when(userRepository.findByRole(UserRole.TEACHER, pageable)).thenReturn(teacherPage);

        Page<User> result = userService.getTeachers(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("teacher1", result.getContent().get(0).getUsername());
        verify(userRepository).findByRole(UserRole.TEACHER, pageable);
    }

    @Test
    void getTeachers_WithSearch_ShouldReturnFilteredTeachers() {
        String search = "teacher";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> teacherPage = new PageImpl<>(List.of(teacher), pageable, 1);
        when(userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.TEACHER, pageable))
                .thenReturn(teacherPage);

        Page<User> result = userService.getTeachers(search, pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findByUsernameOrFirstNameOrLastName(search, UserRole.TEACHER, pageable);
    }

    @Test
    void getStudentsByGroupId_WhenGroupExists_ShouldReturnStudents() {
        when(studyGroupRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findAllByGroupId(1L)).thenReturn(List.of(student, teacher));

        List<User> result = userService.getStudentsByGroupId(1L);

        assertEquals(2, result.size());
        verify(studyGroupRepository).existsById(1L);
        verify(userRepository).findAllByGroupId(1L);
    }

    @Test
    void getStudentsByGroupId_WhenGroupNotExists_ShouldThrowException() {
        when(studyGroupRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getStudentsByGroupId(999L));
        assertEquals("Group not found with id: 999", exception.getMessage());
        verify(studyGroupRepository).existsById(999L);
        verify(userRepository, never()).findAllByGroupId(any());
    }

    @Test
    void getUsersWithTempPasswordByGroupId_WhenGroupExists_ShouldReturnUsersWithTempPassword() {
        when(studyGroupRepository.existsById(2L)).thenReturn(true);
        when(userRepository.findAllByGroupIdAndTempPasswordIsNotNull(2L)).thenReturn(List.of(student));

        List<User> result = userService.getUsersWithTempPasswordByGroupId(2L);

        assertEquals(1, result.size());
        assertEquals("student1", result.get(0).getUsername());
        verify(studyGroupRepository).existsById(2L);
        verify(userRepository).findAllByGroupIdAndTempPasswordIsNotNull(2L);
    }

    @Test
    void getUsersWithTempPasswordByGroupId_WhenGroupNotExists_ShouldThrowException() {
        when(studyGroupRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUsersWithTempPasswordByGroupId(999L));
        assertEquals("Group not found with id: 999", exception.getMessage());
        verify(studyGroupRepository).existsById(999L);
        verify(userRepository, never()).findAllByGroupIdAndTempPasswordIsNotNull(any());
    }

    @Test
    void updateProfile_WhenUserExists_ShouldUpdateProfile() {
        ProfileDto dto = ProfileDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber("987-654-3210")
                .address("456 Oak Ave")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateProfile(1L, dto);

        Profile updatedProfile = result.getProfile();
        assertEquals("Jane", updatedProfile.getFirstName());
        assertEquals("Smith", updatedProfile.getLastName());
        assertEquals("Michael", updatedProfile.getMiddleName());
        assertEquals("987-654-3210", updatedProfile.getPhoneNumber());
        assertEquals("456 Oak Ave", updatedProfile.getAddress());

        verify(userRepository).findById(1L);
        verify(userRepository).save(student);
    }

    @Test
    void updateProfile_WhenUserNotExists_ShouldThrowException() {
        ProfileDto dto = ProfileDto.builder().firstName("Jane").lastName("Smith").build();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateProfile(999L, dto));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        ProfileDto dto = ProfileDto.builder().firstName("Jane").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateProfile(1L, dto);

        Profile updatedProfile = result.getProfile();
        assertEquals("Jane", updatedProfile.getFirstName());
        assertEquals("Doe", updatedProfile.getLastName());
        assertEquals("Michael", updatedProfile.getMiddleName());
        assertEquals("123-456-7890", updatedProfile.getPhoneNumber());
        assertEquals("123 Main St", updatedProfile.getAddress());

        verify(userRepository).save(student);
    }

    @Test
    void updateProfile_WithAllNullFields_ShouldNotChangeAnything() {
        ProfileDto dto = ProfileDto.builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateProfile(1L, dto);

        Profile updatedProfile = result.getProfile();
        assertEquals("John", updatedProfile.getFirstName());
        assertEquals("Doe", updatedProfile.getLastName());
        assertEquals("Michael", updatedProfile.getMiddleName());
        assertEquals("123-456-7890", updatedProfile.getPhoneNumber());
        assertEquals("123 Main St", updatedProfile.getAddress());

        verify(userRepository).save(student);
    }

    @Test
    void resolveProfileTargetId_ForNonAdmin_ShouldReturnRequesterId() {
        ProfileDto dto = ProfileDto.builder().id(999L).build();

        Long result = userService.resolveProfileTargetId(dto, 1L, UserRole.STUDENT);

        assertEquals(1L, result);
    }

    @Test
    void resolveProfileTargetId_ForAdminWithId_ShouldReturnDtoId() {
        ProfileDto dto = ProfileDto.builder().id(42L).build();

        Long result = userService.resolveProfileTargetId(dto, 1L, UserRole.ADMIN);

        assertEquals(42L, result);
    }

    @Test
    void resolveProfileTargetId_ForAdminWithoutId_ShouldThrowException() {
        ProfileDto dto = ProfileDto.builder().build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.resolveProfileTargetId(dto, 1L, UserRole.ADMIN));
        assertEquals("Profile ID must not be null for admin", exception.getMessage());
    }
}
