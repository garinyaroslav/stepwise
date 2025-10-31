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

import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudyGroupService studyGroupService;

    private StudyGroup studyGroup;
    private User student1;
    private User student2;

    @BeforeEach
    void setUp() {
        student1 = User.builder()
                .id(1L)
                .username("student1")
                .role(UserRole.STUDENT)
                .build();

        student2 = User.builder()
                .id(2L)
                .username("student2")
                .role(UserRole.STUDENT)
                .build();

        studyGroup = StudyGroup.builder()
                .id(1L)
                .name("Math Group")
                .students(List.of(student1, student2))
                .build();
    }

    @Test
    void findAll_WithoutSearch_ShouldReturnAllGroups() {
        List<StudyGroup> expectedGroups = List.of(studyGroup);
        when(studyGroupRepository.findAll()).thenReturn(expectedGroups);

        List<StudyGroup> result = studyGroupService.findAll(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studyGroupRepository, times(1)).findAll();
        verify(studyGroupRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void findAll_WithSearch_ShouldReturnFilteredGroups() {
        String search = "math";
        List<StudyGroup> expectedGroups = List.of(studyGroup);
        when(studyGroupRepository.findByNameContainingIgnoreCase(search)).thenReturn(expectedGroups);

        List<StudyGroup> result = studyGroupService.findAll(search);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(studyGroupRepository, times(1)).findByNameContainingIgnoreCase(search);
        verify(studyGroupRepository, never()).findAll();
    }

    @Test
    void findById_WhenGroupExists_ShouldReturnGroup() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));

        StudyGroup result = studyGroupService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Math Group", result.getName());
        verify(studyGroupRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WhenGroupNotExists_ShouldThrowException() {
        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studyGroupService.findById(999L));

        assertEquals("Group with id 999 not found", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(999L);
    }

    @Test
    void create_WithValidStudents_ShouldCreateGroup() {
        List<Long> studentIds = List.of(1L, 2L);
        List<User> students = List.of(student1, student2);

        when(userRepository.findByIdInAndRole(studentIds, UserRole.STUDENT)).thenReturn(students);
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(studyGroup);

        studyGroupService.create("New Group", studentIds);

        verify(userRepository, times(1)).findByIdInAndRole(studentIds, UserRole.STUDENT);
        verify(studyGroupRepository, times(1)).save(any(StudyGroup.class));
    }

    @Test
    void create_WithNoValidStudents_ShouldCreateEmptyGroup() {
        List<Long> studentIds = List.of(999L);

        when(userRepository.findByIdInAndRole(studentIds, UserRole.STUDENT)).thenReturn(List.of());
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(studyGroup);

        studyGroupService.create("Empty Group", studentIds);

        verify(userRepository, times(1)).findByIdInAndRole(studentIds, UserRole.STUDENT);
        verify(studyGroupRepository, times(1)).save(any(StudyGroup.class));
    }

    @Test
    void update_WhenGroupExistsWithValidStudents_ShouldUpdateGroup() {
        List<Long> newStudentIds = List.of(1L);
        List<User> newStudents = List.of(student1);
        StudyGroup updatedGroup = StudyGroup.builder()
                .id(1L)
                .name("Math Group")
                .students(newStudents)
                .build();

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findByIdInAndRole(newStudentIds, UserRole.STUDENT)).thenReturn(newStudents);
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(updatedGroup);

        StudyGroup result = studyGroupService.update(1L, newStudentIds);

        assertNotNull(result);
        assertEquals(1, result.getStudents().size());
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByIdInAndRole(newStudentIds, UserRole.STUDENT);
        verify(studyGroupRepository, times(1)).save(studyGroup);
    }

    @Test
    void update_WhenGroupNotExists_ShouldThrowException() {
        List<Long> newStudentIds = List.of(1L);

        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studyGroupService.update(999L, newStudentIds));

        assertEquals("Group with id 999 not found", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(999L);
        verify(userRepository, never()).findByIdInAndRole(anyList(), any());
        verify(studyGroupRepository, never()).save(any(StudyGroup.class));
    }

    @Test
    void update_WithNoValidStudents_ShouldThrowException() {
        List<Long> newStudentIds = List.of(999L);

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findByIdInAndRole(newStudentIds, UserRole.STUDENT)).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studyGroupService.update(1L, newStudentIds));

        assertEquals("No valid students found for the provided IDs", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByIdInAndRole(newStudentIds, UserRole.STUDENT);
        verify(studyGroupRepository, never()).save(any(StudyGroup.class));
    }
}
