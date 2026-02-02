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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.entity.WorkTemplateChapter;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User student;
    private User teacher;
    private StudyGroup studyGroup;
    private AcademicWork academicWork;
    private WorkTemplate workTemplate;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .username("student1")
                .email("student1@example.com")
                .role(UserRole.STUDENT)
                .build();

        teacher = User.builder()
                .id(2L)
                .username("teacher1")
                .role(UserRole.TEACHER)
                .build();

        studyGroup = StudyGroup.builder()
                .id(1L)
                .name("Group A")
                .students(List.of(student))
                .build();

        List<WorkTemplateChapter> chapters = new ArrayList<>();
        chapters.add(WorkTemplateChapter.builder()
                .id(1L)
                .title("Chapter 1")
                .indexOfChapter(1)
                .build());
        chapters.add(WorkTemplateChapter.builder()
                .id(2L)
                .title("Chapter 2")
                .indexOfChapter(2)
                .build());

        workTemplate = WorkTemplate.builder()
                .id(1L)
                .templateTitle("Research Template")
                .templateDescription("Research description")
                .workTitle("Research Paper")
                .workTemplateChapters(chapters)
                .build();

        academicWork = AcademicWork.builder()
                .id(1L)
                .group(studyGroup)
                .workTemplate(workTemplate)
                .build();

        project = Project.builder()
                .id(1L)
                .title("Project Title")
                .description("Project Description")
                .student(student)
                .academicWork(academicWork)
                .isApprovedForDefense(false)
                .items(new ArrayList<>())
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(
                projectService, "fromEmail", "test@example.com");
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
        assertEquals(1L, result.get(0).getId());
        verify(projectRepository, times(1)).findAllByAcademicWorkId(1L);
    }

    @Test
    void getAllByWorkIdAndStudentId_ShouldReturnProjects() {
        List<Project> expectedProjects = List.of(project);
        when(projectRepository.findAllByAcademicWorkIdAndStudentId(1L, 1L)).thenReturn(expectedProjects);

        List<Project> result = projectService.getAllByWorkIdAndStudentId(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(projectRepository, times(1)).findAllByAcademicWorkIdAndStudentId(1L, 1L);
    }

    @Test
    void approve_WhenAllItemsApproved_ShouldApproveProjectAndSendEmail() {
        Project testProject = Project.builder()
                .id(1L)
                .title("Test Project")
                .academicWork(academicWork)
                .student(student)
                .isApprovedForDefense(false)
                .items(new ArrayList<>())
                .build();

        ExplanatoryNoteItem approvedItem1 = ExplanatoryNoteItem.builder()
                .id(1L)
                .status(ItemStatus.APPROVED)
                .orderNumber(0)
                .fileName("item1.pdf")
                .project(testProject)
                .build();

        ExplanatoryNoteItem approvedItem2 = ExplanatoryNoteItem.builder()
                .id(2L)
                .status(ItemStatus.APPROVED)
                .orderNumber(1)
                .fileName("item2.pdf")
                .project(testProject)
                .build();

        testProject.setItems(List.of(approvedItem1, approvedItem2));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        Project result = projectService.approve(1L);

        assertNotNull(result);
        assertTrue(result.isApprovedForDefense());
        assertNotNull(result.getApprovedForDefenseAt());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(testProject);
        verify(userRepository, times(1)).findById(1L);
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void approve_WhenNotAllItemsApproved_ShouldThrowException() {
        Project testProject = Project.builder()
                .id(1L)
                .academicWork(academicWork)
                .student(student)
                .items(new ArrayList<>())
                .build();

        ExplanatoryNoteItem approvedItem = ExplanatoryNoteItem.builder()
                .id(1L)
                .status(ItemStatus.APPROVED)
                .orderNumber(0)
                .fileName("approved.pdf")
                .project(testProject)
                .build();

        ExplanatoryNoteItem pendingItem = ExplanatoryNoteItem.builder()
                .id(2L)
                .status(ItemStatus.SUBMITTED)
                .orderNumber(1)
                .fileName("pending.pdf")
                .project(testProject)
                .build();

        testProject.setItems(List.of(approvedItem, pendingItem));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(1L));

        assertEquals("Cannot approve project with id: 1", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
        verify(userRepository, never()).findById(anyLong());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void approve_WhenProjectNotExists_ShouldThrowException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(999L));

        assertEquals("Project not found with id: 999", exception.getMessage());
        verify(projectRepository, times(1)).findById(999L);
        verify(projectRepository, never()).save(any(Project.class));
        verify(userRepository, never()).findById(anyLong());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void approve_WhenStudentNotFound_ShouldLogErrorButContinue() {
        Project testProject = Project.builder()
                .id(1L)
                .title("Test Project")
                .academicWork(academicWork)
                .student(student)
                .isApprovedForDefense(false)
                .items(new ArrayList<>())
                .build();

        ExplanatoryNoteItem approvedItem1 = ExplanatoryNoteItem.builder()
                .id(1L)
                .status(ItemStatus.APPROVED)
                .orderNumber(0)
                .fileName("item1.pdf")
                .project(testProject)
                .build();

        ExplanatoryNoteItem approvedItem2 = ExplanatoryNoteItem.builder()
                .id(2L)
                .status(ItemStatus.APPROVED)
                .orderNumber(1)
                .fileName("item2.pdf")
                .project(testProject)
                .build();

        testProject.setItems(List.of(approvedItem1, approvedItem2));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Project result = projectService.approve(1L);

        assertNotNull(result);
        assertTrue(result.isApprovedForDefense());
        assertNotNull(result.getApprovedForDefenseAt());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(testProject);
        verify(userRepository, times(1)).findById(1L);
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void approve_WhenNoItems_ShouldThrowException() {
        Project testProject = Project.builder()
                .id(1L)
                .academicWork(academicWork)
                .student(student)
                .items(new ArrayList<>())
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(1L));

        assertEquals("Cannot approve project with id: 1", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void approve_WhenWorkTemplateHasNoChapters_ShouldThrowException() {
        WorkTemplate emptyTemplate = WorkTemplate.builder()
                .id(2L)
                .templateTitle("Empty Template")
                .templateDescription("No chapters")
                .workTemplateChapters(new ArrayList<>())
                .build();

        AcademicWork work = AcademicWork.builder()
                .id(2L)
                .group(studyGroup)
                .workTemplate(emptyTemplate)
                .build();

        Project testProject = Project.builder()
                .id(2L)
                .academicWork(work)
                .student(student)
                .items(new ArrayList<>())
                .build();

        ExplanatoryNoteItem approvedItem = ExplanatoryNoteItem.builder()
                .id(3L)
                .status(ItemStatus.APPROVED)
                .orderNumber(0)
                .fileName("approved.pdf")
                .project(testProject)
                .build();

        testProject.setItems(List.of(approvedItem));

        when(projectRepository.findById(2L)).thenReturn(Optional.of(testProject));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(2L));

        assertEquals("Cannot approve project with id: 2", exception.getMessage());
        verify(projectRepository, times(1)).findById(2L);
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
