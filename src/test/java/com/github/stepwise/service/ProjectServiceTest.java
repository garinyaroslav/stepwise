package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.github.stepwise.configuration.MailConfigurationProperties;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.ProjectStatus;
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

    @Mock
    private MailConfigurationProperties mailConfig;

    @InjectMocks
    private ProjectService projectService;

    private Project project;
    private User student;
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
                .status(ProjectStatus.IN_PROGRESS)
                .items(new ArrayList<>())
                .build();
    }

    private ExplanatoryNoteItem buildApprovedItem(Long id, int orderNumber, Project project) {
        return ExplanatoryNoteItem.builder()
                .id(id)
                .status(ItemStatus.APPROVED)
                .orderNumber(orderNumber)
                .project(project)
                .history(new ArrayList<>())
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
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void getByProjectId_WhenProjectExists_ShouldReturnProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getByProjectId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getByProjectId_WhenProjectNotExists_ShouldThrowException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.getByProjectId(999L));

        assertEquals("Project not found project id: 999", exception.getMessage());
    }

    @Test
    void getAllByWorkId_ShouldReturnProjects() {
        when(projectRepository.findAllByAcademicWorkId(1L)).thenReturn(List.of(project));

        List<Project> result = projectService.getAllByWorkId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getAllByWorkIdAndStudentId_ShouldReturnProjects() {
        when(projectRepository.findAllByAcademicWorkIdAndStudentId(1L, 1L)).thenReturn(List.of(project));

        List<Project> result = projectService.getAllByWorkIdAndStudentId(1L, 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void approve_WhenAllItemsApproved_ShouldApproveProjectAndSendEmail() {
        Project testProject = Project.builder()
                .id(1L)
                .title("Test Project")
                .academicWork(academicWork)
                .student(student)
                .status(ProjectStatus.IN_PROGRESS)
                .items(new ArrayList<>())
                .build();

        testProject.setItems(List.of(
                buildApprovedItem(1L, 0, testProject),
                buildApprovedItem(2L, 1, testProject)));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        Project result = projectService.approve(1L);

        assertNotNull(result);
        assertEquals(ProjectStatus.APPROVED_FOR_DEFENSE, result.getStatus());
        assertNotNull(result.getApprovedForDefenseAt());
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
                .id(1L).status(ItemStatus.APPROVED).orderNumber(0)
                .project(testProject).history(new ArrayList<>()).build();
        ExplanatoryNoteItem pendingItem = ExplanatoryNoteItem.builder()
                .id(2L).status(ItemStatus.SUBMITTED).orderNumber(1)
                .project(testProject).history(new ArrayList<>()).build();

        testProject.setItems(List.of(approvedItem, pendingItem));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(1L));

        assertEquals("Cannot approve project with id: 1", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void approve_WhenProjectNotExists_ShouldThrowException() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.approve(999L));

        assertEquals("Project not found with id: 999", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void approve_WhenStudentNotFound_ShouldLogErrorButContinue() {
        Project testProject = Project.builder()
                .id(1L)
                .title("Test Project")
                .academicWork(academicWork)
                .student(student)
                .status(ProjectStatus.IN_PROGRESS)
                .items(new ArrayList<>())
                .build();

        testProject.setItems(List.of(
                buildApprovedItem(1L, 0, testProject),
                buildApprovedItem(2L, 1, testProject)));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Project result = projectService.approve(1L);

        assertEquals(ProjectStatus.APPROVED_FOR_DEFENSE, result.getStatus());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void approve_WhenNoItems_ShouldThrowException() {
        Project testProject = Project.builder()
                .id(1L).academicWork(academicWork).student(student).items(new ArrayList<>()).build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        assertThrows(IllegalArgumentException.class, () -> projectService.approve(1L));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void approve_WhenWorkTemplateHasNoChapters_ShouldThrowException() {
        WorkTemplate emptyTemplate = WorkTemplate.builder()
                .id(2L).templateTitle("Empty Template")
                .workTemplateChapters(new ArrayList<>()).build();

        AcademicWork work = AcademicWork.builder()
                .id(2L).group(studyGroup).workTemplate(emptyTemplate).build();

        Project testProject = Project.builder()
                .id(2L).academicWork(work).student(student).items(new ArrayList<>()).build();

        ExplanatoryNoteItem approvedItem = ExplanatoryNoteItem.builder()
                .id(3L).status(ItemStatus.APPROVED).orderNumber(0)
                .project(testProject).history(new ArrayList<>()).build();
        testProject.setItems(List.of(approvedItem));

        when(projectRepository.findById(2L)).thenReturn(Optional.of(testProject));

        assertThrows(IllegalArgumentException.class, () -> projectService.approve(2L));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void isProjectBelongsToStudent_ShouldReturnTrue() {
        when(projectRepository.existsByIdAndStudentId(1L, 1L)).thenReturn(true);
        assertTrue(projectService.isProjectBelongsToStudent(1L, 1L));
    }

    @Test
    void isProjectBelongsToStudent_ShouldReturnFalse() {
        when(projectRepository.existsByIdAndStudentId(1L, 999L)).thenReturn(false);
        assertFalse(projectService.isProjectBelongsToStudent(1L, 999L));
    }

    @Test
    void isProjectBelongsToTeacher_ShouldReturnTrue() {
        when(projectRepository.existsByIdAndTeacherId(1L, 2L)).thenReturn(true);
        assertTrue(projectService.isProjectBelongsToTeacher(1L, 2L));
    }

    @Test
    void isProjectBelongsToTeacher_ShouldReturnFalse() {
        when(projectRepository.existsByIdAndTeacherId(1L, 999L)).thenReturn(false);
        assertFalse(projectService.isProjectBelongsToTeacher(1L, 999L));
    }
}
