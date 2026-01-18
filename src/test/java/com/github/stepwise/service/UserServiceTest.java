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
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;

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
    private StudyGroup studyGroup;
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

        studyGroup = StudyGroup.builder()
                .id(1L)
                .name("Math Group")
                .students(List.of(student, teacher))
                .build();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        User result = userService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("student1", result.getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.findById(999L));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getAllStudents_ShouldReturnStudentsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> studentPage = new PageImpl<>(List.of(student), pageable, 1);
        when(userRepository.findByRole(UserRole.STUDENT, pageable)).thenReturn(studentPage);

        Page<User> result = userService.getAllStudents(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("student1", result.getContent().get(0).getUsername());
        verify(userRepository, times(1)).findByRole(UserRole.STUDENT, pageable);
    }

    @Test
    void getAllStudents_WithSearch_ShouldReturnFilteredStudents() {
        String search = "john";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> studentPage = new PageImpl<>(List.of(student), pageable, 1);
        when(userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.STUDENT, pageable))
                .thenReturn(studentPage);

        Page<User> result = userService.getAllStudents(search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1))
                .findByUsernameOrFirstNameOrLastName(search, UserRole.STUDENT, pageable);
    }

    @Test
    void getAllTeachers_ShouldReturnTeachersPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> teacherPage = new PageImpl<>(List.of(teacher), pageable, 1);
        when(userRepository.findByRole(UserRole.TEACHER, pageable)).thenReturn(teacherPage);

        Page<User> result = userService.getAllTeachers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("teacher1", result.getContent().get(0).getUsername());
        verify(userRepository, times(1)).findByRole(UserRole.TEACHER, pageable);
    }

    @Test
    void getAllTeachers_WithSearch_ShouldReturnFilteredTeachers() {
        String search = "teacher";
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> teacherPage = new PageImpl<>(List.of(teacher), pageable, 1);
        when(userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.TEACHER, pageable))
                .thenReturn(teacherPage);

        Page<User> result = userService.getAllTeachers(search, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(userRepository, times(1))
                .findByUsernameOrFirstNameOrLastName(search, UserRole.TEACHER, pageable);
    }

    @Test
    void getAllStudentsByGroupId_WhenGroupExists_ShouldReturnStudents() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findAllById(anyList())).thenReturn(List.of(student, teacher));

        List<User> result = userService.getAllStudentsByGroupId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findAllById(anyList());
    }

    @Test
    void getAllStudentsByGroupId_WhenGroupNotExists_ShouldThrowException() {
        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.getAllStudentsByGroupId(999L));
        assertEquals("Group not found with id: 999", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(999L);
        verify(userRepository, never()).findAllById(anyList());
    }

    @Test
    void getUsersWithTempPasswordByGroupId_ShouldReturnUsersWithTempPassword() {
        User studentWithTempPassword = User.builder()
                .id(3L)
                .username("student3")
                .role(UserRole.STUDENT)
                .tempPassword("pass")
                .build();

        User studentWithoutTempPassword = User.builder()
                .id(4L)
                .username("student4")
                .role(UserRole.STUDENT)
                .tempPassword(null)
                .build();

        StudyGroup groupWithMixedUsers = StudyGroup.builder()
                .id(2L)
                .name("Science Group")
                .students(List.of(studentWithTempPassword, studentWithoutTempPassword))
                .build();

        when(studyGroupRepository.findById(2L)).thenReturn(Optional.of(groupWithMixedUsers));

        List<User> result = userService.getUsersWithTempPasswordByGroupId(2L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("student3", result.get(0).getUsername());
        assertTrue(result.get(0).getTempPassword() != null);
        verify(studyGroupRepository, times(1)).findById(2L);
    }

    @Test
    void getUsersWithTempPasswordByGroupId_WhenGroupNotExists_ShouldThrowException() {
        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.getUsersWithTempPasswordByGroupId(999L));
        assertEquals("Group not found with id: 999", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(999L);
    }

    @Test
    void updateProfile_WhenUserExists_ShouldUpdateProfile() {
        String newFirstName = "Jane";
        String newLastName = "Smith";
        String newPhoneNumber = "987-654-3210";
        String newAddress = "456 Oak Ave";

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateProfile(1L, newFirstName, newLastName, null, newPhoneNumber, newAddress);

        assertNotNull(result);
        Profile updatedProfile = result.getProfile();
        assertEquals(newFirstName, updatedProfile.getFirstName());
        assertEquals(newLastName, updatedProfile.getLastName());
        assertEquals("Michael", updatedProfile.getMiddleName());
        assertEquals(newPhoneNumber, updatedProfile.getPhoneNumber());
        assertEquals(newAddress, updatedProfile.getAddress());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(student);
    }

    @Test
    void updateProfile_WhenUserNotExists_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(999L, "Jane", "Smith", null, null, null));
        assertEquals("User not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        String newFirstName = "Jane";

        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateProfile(1L, newFirstName, null, null, null, null);

        assertNotNull(result);
        Profile updatedProfile = result.getProfile();
        assertEquals(newFirstName, updatedProfile.getFirstName());
        assertEquals("Doe", updatedProfile.getLastName());
        assertEquals("Michael", updatedProfile.getMiddleName());
        assertEquals("123-456-7890", updatedProfile.getPhoneNumber());
        assertEquals("123 Main St", updatedProfile.getAddress());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(student);
    }

    @Test
    void updateProfile_WithNullValues_ShouldNotUpdateFields() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateProfile(1L, null, null, null, null, null);

        assertNotNull(result);
        Profile updatedProfile = result.getProfile();
        assertEquals("John", updatedProfile.getFirstName());
        assertEquals("Doe", updatedProfile.getLastName());
        assertEquals("Michael", updatedProfile.getMiddleName());
        assertEquals("123-456-7890", updatedProfile.getPhoneNumber());
        assertEquals("123 Main St", updatedProfile.getAddress());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(student);
    }
}
