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

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.AcademicWorkChapter;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User student;
    private User teacher;
    private AcademicWork academicWork;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .username("student1")
                .role(UserRole.STUDENT)
                .build();

        teacher = User.builder()
                .id(2L)
                .username("teacher1")
                .role(UserRole.TEACHER)
                .build();

        AcademicWorkChapter chapter1 = AcademicWorkChapter.builder()
                .title("Chapter 1")
                .build();

        AcademicWorkChapter chapter2 = AcademicWorkChapter.builder()
                .title("Chapter 2")
                .build();

        academicWork = AcademicWork.builder()
                .id(1L)
                .title("Research Paper")
                .academicWorkChapters(List.of(chapter1, chapter2))
                .teacher(teacher)
                .build();

        project = Project.builder()
                .id(1L)
                .title("Project Title")
                .description("Project Description")
                .student(student)
                .academicWork(academicWork)
                .isApprovedForDefense(false)
                .build();
    }

    @Test
    void updateProject_WhenProjectExists_ShouldUpdateProject() {
        Project newProjectData = Project.builder()
                .id(1L)
                .title("Updated Title")
                .description("Updated Description")
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.updateProject(newProjectData);

        assertNotNull(result);
        assertEquals("Updated Title", project.getTitle());
        assertEquals("Updated Description", project.getDescription());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void updateProject_WhenProjectNotExists_ShouldThrowException() {
        Project newProjectData = Project.builder()
                .id(999L)
                .title("Updated Title")
                .description("Updated Description")
                .build();

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.updateProject(newProjectData));

        assertEquals("Project not found with id: 999", exception.getMessage());
        verify(projectRepository, times(1)).findById(999L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getByProjectId_WhenProjectExists_ShouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getByProjectId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    void getByProjectId_WhenProjectNotExists_ShouldThrowException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.getByProjectId(999L));

        assertEquals("Project not found project id: 999", exception.getMessage());
        verify(projectRepository, times(1)).findById(999L);
    }

    @Test
    void getAllByWorkId_ShouldReturnProjects() {
        List<Project> expectedProjects = List.of(project);
        when(projectRepository.findAllByAcademicWorkId(1L)).thenReturn(expectedProjects);

        List<Project> result = projectService.getAllByWorkId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findAllByAcademicWorkId(1L);
    }

    @Test
    void getAllByWorkIdAndStudentId_ShouldReturnProjects() {
        List<Project> expectedProjects = List.of(project);
        when(projectRepository.findAllByAcademicWorkIdAndStudentId(1L, 1L)).thenReturn(expectedProjects);

        List<Project> result = projectService.getAllByWorkIdAndStudentId(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(projectRepository, times(1)).findAllByAcademicWorkIdAndStudentId(1L, 1L);
    }

    @Test
    void approve_WhenAllItemsApproved_ShouldApproveProject() {
        ExplanatoryNoteItem approvedItem1 = ExplanatoryNoteItem.builder()
                .status(ItemStatus.APPROVED)
                .build();

        ExplanatoryNoteItem approvedItem2 = ExplanatoryNoteItem.builder()
                .status(ItemStatus.APPROVED)
                .build();

        project.setItems(List.of(approvedItem1, approvedItem2));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project result = projectService.approve(1L);

        assertNotNull(result);
        assertTrue(result.isApprovedForDefense());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void approve_WhenNotAllItemsApproved_ShouldThrowException() {
        ExplanatoryNoteItem approvedItem = ExplanatoryNoteItem.builder()
                .status(ItemStatus.APPROVED)
                .build();

        ExplanatoryNoteItem pendingItem = ExplanatoryNoteItem.builder()
                .status(ItemStatus.SUBMITTED)
                .build();

        project.setItems(List.of(approvedItem, pendingItem));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(1L));

        assertEquals("Cannot approve project with id: 1", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void approve_WhenProjectNotExists_ShouldThrowException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(999L));

        assertEquals("Project not found with id: 999", exception.getMessage());
        verify(projectRepository, times(1)).findById(999L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void isProjectBelongsToStudent_ShouldReturnTrue() {
        when(projectRepository.existsByIdAndStudentId(1L, 1L)).thenReturn(true);

        boolean result = projectService.isProjectBelongsToStudent(1L, 1L);

        assertTrue(result);
        verify(projectRepository, times(1)).existsByIdAndStudentId(1L, 1L);
    }

    @Test
    void isProjectBelongsToStudent_ShouldReturnFalse() {
        when(projectRepository.existsByIdAndStudentId(1L, 999L)).thenReturn(false);

        boolean result = projectService.isProjectBelongsToStudent(1L, 999L);

        assertFalse(result);
        verify(projectRepository, times(1)).existsByIdAndStudentId(1L, 999L);
    }

    @Test
    void isProjectBelongsToTeacher_ShouldReturnTrue() {
        when(projectRepository.existsByIdAndTeacherId(1L, 2L)).thenReturn(true);

        boolean result = projectService.isProjectBelongsToTeacher(1L, 2L);

        assertTrue(result);
        verify(projectRepository, times(1)).existsByIdAndTeacherId(1L, 2L);
    }

    @Test
    void isProjectBelongsToTeacher_ShouldReturnFalse() {
        when(projectRepository.existsByIdAndTeacherId(1L, 999L)).thenReturn(false);

        boolean result = projectService.isProjectBelongsToTeacher(1L, 999L);

        assertFalse(result);
        verify(projectRepository, times(1)).existsByIdAndTeacherId(1L, 999L);
    }
}
