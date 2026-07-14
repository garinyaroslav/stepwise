package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.web.dto.GroupResponseDto;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AcademicWorkRepository academicWorkRepository;

    @Mock
    private ProjectRepository projectRepository;

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
    void findAllSummaries_WithoutSearch_ShouldReturnAllGroupSummaries() {
        List<GroupResponseDto> expected = List.of(new GroupResponseDto(1L, "Math Group", 2));
        when(studyGroupRepository.findAllSummaries()).thenReturn(expected);

        List<GroupResponseDto> result = studyGroupService.findAllSummaries(null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getStudentsCount());
        verify(studyGroupRepository, times(1)).findAllSummaries();
        verify(studyGroupRepository, never()).findSummariesByNameContaining(anyString());
    }

    @Test
    void findAllSummaries_WithBlankSearch_ShouldReturnAllGroupSummaries() {
        List<GroupResponseDto> expected = List.of(new GroupResponseDto(1L, "Math Group", 2));
        when(studyGroupRepository.findAllSummaries()).thenReturn(expected);

        List<GroupResponseDto> result = studyGroupService.findAllSummaries("   ");

        assertEquals(1, result.size());
        verify(studyGroupRepository, times(1)).findAllSummaries();
        verify(studyGroupRepository, never()).findSummariesByNameContaining(anyString());
    }

    @Test
    void findAllSummaries_WithSearch_ShouldReturnFilteredGroupSummaries() {
        String search = "math";
        List<GroupResponseDto> expected = List.of(new GroupResponseDto(1L, "Math Group", 2));
        when(studyGroupRepository.findSummariesByNameContaining(search)).thenReturn(expected);

        List<GroupResponseDto> result = studyGroupService.findAllSummaries(search);

        assertEquals(1, result.size());
        verify(studyGroupRepository, times(1)).findSummariesByNameContaining(search);
        verify(studyGroupRepository, never()).findAllSummaries();
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

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> studyGroupService.findById(999L));

        assertEquals("Group not found with id: 999", exception.getMessage());
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
        when(academicWorkRepository.findByGroupId(1L)).thenReturn(List.of());
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(updatedGroup);

        StudyGroup result = studyGroupService.update(1L, newStudentIds);

        assertNotNull(result);
        assertEquals(1, result.getStudents().size());
        assertEquals(1L, result.getStudents().get(0).getId());

        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByIdInAndRole(newStudentIds, UserRole.STUDENT);
        verify(academicWorkRepository, times(1)).findByGroupId(1L);
        verify(projectRepository, never()).findByAcademicWorkInAndStudentIdIn(any(), any());
        verify(projectRepository, never()).deleteAll(any());
        verify(projectRepository, never()).saveAll(any());
        verify(studyGroupRepository, times(1)).save(studyGroup);
    }

    @Test
    void update_WhenGroupNotExists_ShouldThrowException() {
        List<Long> newStudentIds = List.of(1L);

        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> studyGroupService.update(999L, newStudentIds));

        assertEquals("Group not found with id: 999", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(999L);
        verify(userRepository, never()).findByIdInAndRole(anyList(), any());
        verify(academicWorkRepository, never()).findByGroupId(anyLong());
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
        verify(academicWorkRepository, never()).findByGroupId(anyLong());
        verify(studyGroupRepository, never()).save(any(StudyGroup.class));
    }

    @Test
    void update_WithEmptyStudentList_ShouldThrowException() {
        List<Long> newStudentIds = List.of();

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findByIdInAndRole(newStudentIds, UserRole.STUDENT)).thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> studyGroupService.update(1L, newStudentIds));

        assertEquals("No valid students found for the provided IDs", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByIdInAndRole(newStudentIds, UserRole.STUDENT);
        verify(academicWorkRepository, never()).findByGroupId(anyLong());
        verify(studyGroupRepository, never()).save(any(StudyGroup.class));
    }

    @Test
    void update_WhenRemovingStudentsWithProjects_ShouldDeleteProjects() {
        List<Long> newStudentIds = List.of(1L);
        List<User> newStudents = List.of(student1);

        AcademicWork academicWork = AcademicWork.builder()
                .id(1L)
                .build();
        List<AcademicWork> works = List.of(academicWork);

        Project projectToDelete = Project.builder()
                .id(1L)
                .student(student2)
                .academicWork(academicWork)
                .build();
        List<Project> projectsToDelete = List.of(projectToDelete);

        StudyGroup updatedGroup = StudyGroup.builder()
                .id(1L)
                .name("Math Group")
                .students(newStudents)
                .build();

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findByIdInAndRole(newStudentIds, UserRole.STUDENT)).thenReturn(newStudents);
        when(academicWorkRepository.findByGroupId(1L)).thenReturn(works);
        when(projectRepository.findByAcademicWorkInAndStudentIdIn(works, List.of(2L)))
                .thenReturn(projectsToDelete);
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(updatedGroup);

        StudyGroup result = studyGroupService.update(1L, newStudentIds);

        assertNotNull(result);
        assertEquals(1, result.getStudents().size());

        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByIdInAndRole(newStudentIds, UserRole.STUDENT);
        verify(academicWorkRepository, times(1)).findByGroupId(1L);
        verify(projectRepository, times(1)).findByAcademicWorkInAndStudentIdIn(works, List.of(2L));
        verify(projectRepository, times(1)).deleteAll(projectsToDelete);
        verify(projectRepository, never()).saveAll(any());
        verify(studyGroupRepository, times(1)).save(studyGroup);
    }

    @Test
    void update_WhenAddingStudentsWithExistingWorks_ShouldCreateProjects() {
        List<Long> newStudentIds = List.of(1L, 2L, 3L);

        User student3 = User.builder()
                .id(3L)
                .username("student3")
                .role(UserRole.STUDENT)
                .build();
        List<User> newStudents = List.of(student1, student2, student3);

        var workTemplate = com.github.stepwise.entity.WorkTemplate.builder()
                .workTitle("Тема 1")
                .build();
        AcademicWork academicWork = AcademicWork.builder()
                .id(1L)
                .workTemplate(workTemplate)
                .build();
        List<AcademicWork> works = List.of(academicWork);

        StudyGroup updatedGroup = StudyGroup.builder()
                .id(1L)
                .name("Math Group")
                .students(newStudents)
                .build();

        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findByIdInAndRole(newStudentIds, UserRole.STUDENT)).thenReturn(newStudents);
        when(academicWorkRepository.findByGroupId(1L)).thenReturn(works);
        when(studyGroupRepository.save(any(StudyGroup.class))).thenReturn(updatedGroup);

        StudyGroup result = studyGroupService.update(1L, newStudentIds);

        assertNotNull(result);
        verify(projectRepository, times(1)).saveAll(argThat(projects -> {
            List<Project> list = (List<Project>) projects;
            return list.size() == 1 && list.get(0).getStudent().getId().equals(3L);
        }));
        verify(projectRepository, never()).deleteAll(any());
        verify(studyGroupRepository, times(1)).save(studyGroup);
    }

}
