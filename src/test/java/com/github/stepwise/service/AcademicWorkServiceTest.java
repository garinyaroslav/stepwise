package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.AcademicWorkChapter;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AcademicWorkServiceTest {

    @Mock
    private AcademicWorkRepository academicWorkRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private AcademicWorkService academicWorkService;

    private StudyGroup studyGroup;
    private User teacher;
    private User student1;
    private User student2;
    private AcademicWork academicWork;
    private List<AcademicWorkChapter> chapters;

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

        teacher = User.builder()
                .id(3L)
                .username("teacher1")
                .role(UserRole.TEACHER)
                .build();

        studyGroup = StudyGroup.builder()
                .id(1L)
                .name("Math Group")
                .students(new ArrayList<>(List.of(student1, student2)))
                .build();

        academicWork = AcademicWork.builder()
                .id(1L)
                .title("Research Paper")
                .description("A research paper on mathematics")
                .build();

        AcademicWorkChapter chapter1 = AcademicWorkChapter.builder()
                .title("Introduction")
                .description("Introduction content")
                .build();

        AcademicWorkChapter chapter2 = AcademicWorkChapter.builder()
                .title("Methodology")
                .description("Methodology content")
                .build();

        chapters = List.of(chapter1, chapter2);
    }

    @Test
    void create_WhenGroupAndTeacherExist_ShouldCreateAcademicWorkAndProjects() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(academicWorkRepository.save(any(AcademicWork.class))).thenReturn(academicWork);
        when(projectRepository.saveAll(anyList())).thenReturn(List.of());

        academicWorkService.create(academicWork, chapters, 1L, 3L);

        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(3L);
        verify(academicWorkRepository, times(1)).save(academicWork);
        verify(projectRepository, times(1)).saveAll(anyList());

        assertEquals(studyGroup, academicWork.getGroup());
        assertEquals(teacher, academicWork.getTeacher());
        assertEquals(chapters, academicWork.getAcademicWorkChapters());
    }

    @Test
    void create_WhenGroupNotFound_ShouldThrowException() {
        when(studyGroupRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> academicWorkService.create(academicWork, chapters, 999L, 3L));

        assertEquals("Group not found with id: 999", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(999L);
        verify(userRepository, never()).findById(anyLong());
        verify(academicWorkRepository, never()).save(any(AcademicWork.class));
        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void create_WhenTeacherNotFound_ShouldThrowException() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> academicWorkService.create(academicWork, chapters, 1L, 999L));

        assertEquals("Teacher not found with id: 999", exception.getMessage());
        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(999L);
        verify(academicWorkRepository, never()).save(any(AcademicWork.class));
        verify(projectRepository, never()).saveAll(anyList());
    }

    @Test
    void create_ShouldCreateProjectsForAllStudentsInGroup() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(academicWorkRepository.save(any(AcademicWork.class))).thenReturn(academicWork);

        List<Project> savedProjects = new ArrayList<>();
        when(projectRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Project> projects = invocation.getArgument(0);
            savedProjects.addAll(projects);
            return projects;
        });

        academicWorkService.create(academicWork, chapters, 1L, 3L);

        assertEquals(2, savedProjects.size());

        Project project1 = savedProjects.get(0);
        assertEquals("Мой проект по теме: " + academicWork.getTitle(), project1.getTitle());
        assertEquals("Описание проекта", project1.getDescription());
        assertEquals(academicWork, project1.getAcademicWork());

        Project project2 = savedProjects.get(1);
        assertEquals("Мой проект по теме: " + academicWork.getTitle(), project2.getTitle());
        assertEquals("Описание проекта", project2.getDescription());
        assertEquals(academicWork, project2.getAcademicWork());
    }

    @Test
    void getByGroupId_ShouldReturnAcademicWorks() {
        List<AcademicWork> expectedWorks = List.of(academicWork);
        when(academicWorkRepository.findByGroupId(1L)).thenReturn(expectedWorks);

        List<AcademicWork> result = academicWorkService.getByGroupId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(academicWork, result.get(0));
        verify(academicWorkRepository, times(1)).findByGroupId(1L);
    }

    @Test
    void getByGroupId_WhenNoWorksFound_ShouldReturnEmptyList() {
        when(academicWorkRepository.findByGroupId(2L)).thenReturn(List.of());

        List<AcademicWork> result = academicWorkService.getByGroupId(2L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(academicWorkRepository, times(1)).findByGroupId(2L);
    }

    @Test
    void getById_WhenWorkExists_ShouldReturnAcademicWork() {
        when(academicWorkRepository.findById(1L)).thenReturn(Optional.of(academicWork));

        AcademicWork result = academicWorkService.getById(1L);

        assertNotNull(result);
        assertEquals(academicWork, result);
        verify(academicWorkRepository, times(1)).findById(1L);
    }

    @Test
    void getById_WhenWorkNotExists_ShouldThrowException() {
        when(academicWorkRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> academicWorkService.getById(999L));

        assertEquals("Work not found with id: 999", exception.getMessage());
        verify(academicWorkRepository, times(1)).findById(999L);
    }

    @Test
    void getByStudentId_WhenStudentExists_ShouldReturnAcademicWorks() {
        List<AcademicWork> expectedWorks = List.of(academicWork);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(academicWorkRepository.findByStudentId(1L)).thenReturn(expectedWorks);

        List<AcademicWork> result = academicWorkService.getByStudentId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(academicWork, result.get(0));
        verify(userRepository, times(1)).findById(1L);
        verify(academicWorkRepository, times(1)).findByStudentId(1L);
    }

    @Test
    void getByStudentId_WhenStudentNotExists_ShouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> academicWorkService.getByStudentId(999L));

        assertEquals("Student not found with id: 999", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(academicWorkRepository, never()).findByStudentId(anyLong());
    }

    @Test
    void getByStudentId_WhenNoWorksFound_ShouldReturnEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(student1));
        when(academicWorkRepository.findByStudentId(1L)).thenReturn(List.of());

        List<AcademicWork> result = academicWorkService.getByStudentId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findById(1L);
        verify(academicWorkRepository, times(1)).findByStudentId(1L);
    }

    @Test
    void create_WithEmptyStudentList_ShouldNotCreateProjects() {
        StudyGroup emptyGroup = StudyGroup.builder()
                .id(3L)
                .name("Empty Group")
                .students(new ArrayList<>())
                .build();

        when(studyGroupRepository.findById(3L)).thenReturn(Optional.of(emptyGroup));
        when(userRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(academicWorkRepository.save(any(AcademicWork.class))).thenReturn(academicWork);

        academicWorkService.create(academicWork, chapters, 3L, 3L);

        verify(studyGroupRepository, times(1)).findById(3L);
        verify(userRepository, times(1)).findById(3L);
        verify(academicWorkRepository, times(1)).save(academicWork);
        verify(projectRepository, times(1)).saveAll(List.of());
    }

    @Test
    void create_WithNullChapters_ShouldHandleGracefully() {
        when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(studyGroup));
        when(userRepository.findById(3L)).thenReturn(Optional.of(teacher));
        when(academicWorkRepository.save(any(AcademicWork.class))).thenReturn(academicWork);
        when(projectRepository.saveAll(anyList())).thenReturn(List.of());

        academicWorkService.create(academicWork, null, 1L, 3L);

        verify(studyGroupRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(3L);
        verify(academicWorkRepository, times(1)).save(academicWork);
        verify(projectRepository, times(1)).saveAll(anyList());

        assertNull(academicWork.getAcademicWorkChapters());
    }
}
