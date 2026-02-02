package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.github.stepwise.configuration.FileUploadConfig;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemHistory;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.repository.ExplanatoryNoteRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ExplanatoryNoteItemServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ExplanatoryNoteRepository explanatoryNoteRepository;

    @Mock
    private FileUploadConfig fileUploadConfig;

    @Mock
    private StorageService storageService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExplanatoryNoteItemService explanatoryNoteItemService;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private InputStream inputStream;

    private User student;
    private User teacher;
    private Project project;
    private AcademicWork academicWork;
    private WorkTemplate workTemplate;
    private ExplanatoryNoteItem draftItem;
    private ExplanatoryNoteItem submittedItem;
    private ExplanatoryNoteItem approvedItem;

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

        workTemplate = WorkTemplate.builder()
                .id(1L)
                .countOfChapters(3)
                .build();

        academicWork = AcademicWork.builder()
                .id(1L)
                .workTemplate(workTemplate)
                .build();

        project = Project.builder()
                .id(1L)
                .title("Project Title")
                .student(student)
                .academicWork(academicWork)
                .items(new ArrayList<>())
                .build();

        draftItem = ExplanatoryNoteItem.builder()
                .id(1L)
                .orderNumber(0)
                .status(ItemStatus.DRAFT)
                .fileName("draft.pdf")
                .history(new ArrayList<>())
                .project(project)
                .build();

        ItemHistory draftHistory = ItemHistory.builder()
                .item(draftItem)
                .previousStatus(null)
                .newStatus(ItemStatus.DRAFT)
                .changedAt(LocalDateTime.now())
                .changedBy(student)
                .build();
        draftItem.getHistory().add(draftHistory);

        submittedItem = ExplanatoryNoteItem.builder()
                .id(2L)
                .orderNumber(1)
                .status(ItemStatus.SUBMITTED)
                .fileName("submitted.pdf")
                .history(new ArrayList<>())
                .project(project)
                .build();

        ItemHistory submittedHistory = ItemHistory.builder()
                .item(submittedItem)
                .previousStatus(ItemStatus.DRAFT)
                .newStatus(ItemStatus.SUBMITTED)
                .changedAt(LocalDateTime.now())
                .changedBy(student)
                .build();
        submittedItem.getHistory().add(submittedHistory);

        approvedItem = ExplanatoryNoteItem.builder()
                .id(3L)
                .orderNumber(2)
                .status(ItemStatus.APPROVED)
                .fileName("approved.pdf")
                .history(new ArrayList<>())
                .project(project)
                .build();

        ItemHistory approvedHistory = ItemHistory.builder()
                .item(approvedItem)
                .previousStatus(ItemStatus.SUBMITTED)
                .newStatus(ItemStatus.APPROVED)
                .changedAt(LocalDateTime.now())
                .changedBy(teacher)
                .build();
        approvedItem.getHistory().add(approvedHistory);
    }

    @Test
    void draftItem_WithFirstItem_ShouldCreateNewItem() throws Exception {
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getOriginalFilename()).thenReturn("document.pdf");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        Project savedProject = Project.builder()
                .id(1L)
                .title("Project Title")
                .student(student)
                .academicWork(academicWork)
                .items(new ArrayList<>())
                .build();

        ExplanatoryNoteItem newItem = ExplanatoryNoteItem.builder()
                .id(10L)
                .orderNumber(0)
                .status(ItemStatus.DRAFT)
                .fileName("document.pdf")
                .history(new ArrayList<>())
                .project(savedProject)
                .build();

        savedProject.getItems().add(newItem);

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        doNothing().when(storageService).uploadExplanatoryFile(eq(1L), eq(1L), anyLong(), eq(multipartFile));

        explanatoryNoteItemService.draftItem(1L, 1L, multipartFile);

        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(storageService, times(1)).uploadExplanatoryFile(eq(1L), eq(1L), anyLong(), eq(multipartFile));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void draftItem_WhenProjectNotFound_ShouldThrowException() throws Exception {
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.draftItem(1L, 999L, multipartFile));

        assertEquals("Project not found with id: 999", exception.getMessage());
        verify(projectRepository, times(1)).findById(999L);
        verify(projectRepository, never()).save(any(Project.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void draftItem_WhenUserNotOwner_ShouldThrowException() throws Exception {
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.draftItem(999L, 1L, multipartFile));

        assertEquals("User with id 999 is not the owner of project with id: 1", exception.getMessage());
        verify(projectRepository, times(1)).findById(1L);
        verify(projectRepository, never()).save(any(Project.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void draftItem_WithInvalidFileType_ShouldThrowException() throws Exception {
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("image/jpeg");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.draftItem(1L, 1L, multipartFile));

        assertTrue(exception.getMessage().contains("Only allowed file types are:"));
        verify(projectRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void draftItem_WhenLastItemIsDraft_ShouldUpdateExistingItem() throws Exception {
        project.getItems().add(draftItem);

        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getOriginalFilename()).thenReturn("updated.pdf");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        explanatoryNoteItemService.draftItem(1L, 1L, multipartFile);

        verify(projectRepository, times(1)).save(project);
        verify(userRepository, times(1)).findById(1L);
        assertEquals(1, project.getItems().size());
        assertEquals("updated.pdf", draftItem.getFileName());
        assertEquals(ItemStatus.DRAFT, draftItem.getStatus());
    }

    @Test
    void draftItem_WhenLastItemIsRejected_ShouldUpdateExistingItem() throws Exception {
        ExplanatoryNoteItem rejectedItem = ExplanatoryNoteItem.builder()
                .id(4L)
                .orderNumber(0)
                .status(ItemStatus.REJECTED)
                .fileName("rejected.pdf")
                .history(new ArrayList<>())
                .project(project)
                .build();
        project.getItems().add(rejectedItem);

        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getOriginalFilename()).thenReturn("updated.pdf");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        explanatoryNoteItemService.draftItem(1L, 1L, multipartFile);

        verify(projectRepository, times(1)).save(project);
        verify(userRepository, times(1)).findById(1L);
        assertEquals(1, project.getItems().size());
        assertEquals("updated.pdf", rejectedItem.getFileName());
        assertEquals(ItemStatus.DRAFT, rejectedItem.getStatus());
    }

    @Test
    void draftItem_WhenLastItemIsSubmitted_ShouldThrowException() throws Exception {
        project.getItems().add(submittedItem);

        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.draftItem(1L, 1L, multipartFile));

        assertEquals("Cannot submit more than one item at a time", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void submitItem_WithDraftItem_ShouldSubmitSuccessfully() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(explanatoryNoteRepository.save(any(ExplanatoryNoteItem.class))).thenReturn(draftItem);

        explanatoryNoteItemService.submitItem(1L, 1L);

        verify(explanatoryNoteRepository, times(1)).findById(1L);
        verify(explanatoryNoteRepository, times(1)).save(draftItem);
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void submitItem_WhenItemNotFound_ShouldThrowException() {
        when(explanatoryNoteRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.submitItem(999L, 1L));

        assertEquals("Explanatory note item not found with id: 999", exception.getMessage());
        verify(explanatoryNoteRepository, times(1)).findById(999L);
        verify(explanatoryNoteRepository, never()).save(any(ExplanatoryNoteItem.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void submitItem_WhenItemNotInDraft_ShouldThrowException() {
        when(explanatoryNoteRepository.findById(2L)).thenReturn(Optional.of(submittedItem));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.submitItem(2L, 1L));

        assertTrue(exception.getMessage().contains("is not in DRAFT status"));
        verify(explanatoryNoteRepository, times(1)).findById(2L);
        verify(explanatoryNoteRepository, never()).save(any(ExplanatoryNoteItem.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void approveItem_WithSubmittedItem_ShouldApproveSuccessfully() {
        ExplanatoryNoteItem testSubmittedItem = ExplanatoryNoteItem.builder()
                .id(5L)
                .orderNumber(1)
                .status(ItemStatus.SUBMITTED)
                .fileName("test.pdf")
                .history(new ArrayList<>())
                .project(project)
                .build();

        when(explanatoryNoteRepository.findById(5L)).thenReturn(Optional.of(testSubmittedItem));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(explanatoryNoteRepository.save(any(ExplanatoryNoteItem.class))).thenReturn(testSubmittedItem);

        explanatoryNoteItemService.approveItem(5L, 2L, "Good job");

        verify(explanatoryNoteRepository, times(1)).findById(5L);
        verify(explanatoryNoteRepository, times(1)).save(testSubmittedItem);
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void approveItem_WhenItemNotInSubmitted_ShouldThrowException() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.approveItem(1L, 2L, "Comment"));

        assertTrue(exception.getMessage().contains("is not in SUBMITTED status"));
        verify(explanatoryNoteRepository, times(1)).findById(1L);
        verify(explanatoryNoteRepository, never()).save(any(ExplanatoryNoteItem.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void rejectItem_WithSubmittedItem_ShouldRejectSuccessfully() {
        ExplanatoryNoteItem testSubmittedItem = ExplanatoryNoteItem.builder()
                .id(6L)
                .orderNumber(1)
                .status(ItemStatus.SUBMITTED)
                .fileName("test.pdf")
                .history(new ArrayList<>())
                .project(project)
                .build();

        String teacherComment = "Needs more details";
        when(explanatoryNoteRepository.findById(6L)).thenReturn(Optional.of(testSubmittedItem));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        when(explanatoryNoteRepository.save(any(ExplanatoryNoteItem.class))).thenReturn(testSubmittedItem);

        explanatoryNoteItemService.rejectItem(6L, 2L, teacherComment);

        verify(explanatoryNoteRepository, times(1)).findById(6L);
        verify(explanatoryNoteRepository, times(1)).save(testSubmittedItem);
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void rejectItem_WhenItemNotInSubmitted_ShouldThrowException() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.rejectItem(1L, 2L, "Comment"));

        assertTrue(exception.getMessage().contains("is not in SUBMITTED status"));
        verify(explanatoryNoteRepository, times(1)).findById(1L);
        verify(explanatoryNoteRepository, never()).save(any(ExplanatoryNoteItem.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getItemFile_ShouldReturnInputStream() throws Exception {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));
        when(storageService.downloadExplanatoryFile(1L, 1L, 1L, "draft.pdf")).thenReturn(inputStream);

        InputStream result = explanatoryNoteItemService.getItemFile(1L, 1L, 1L);

        assertNotNull(result);
        assertEquals(inputStream, result);
        verify(explanatoryNoteRepository, times(1)).findById(1L);
        verify(storageService, times(1)).downloadExplanatoryFile(1L, 1L, 1L, "draft.pdf");
    }

    @Test
    void getItemFile_WhenItemNotFound_ShouldThrowException() throws Exception {
        when(explanatoryNoteRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.getItemFile(1L, 1L, 999L));

        assertEquals("Explanatory note item not found with id: 999", exception.getMessage());
        verify(explanatoryNoteRepository, times(1)).findById(999L);
        verify(storageService, never()).downloadExplanatoryFile(anyLong(), anyLong(), anyLong(), anyString());
    }

    @Test
    void getItemFile_WhenFileNotFound_ShouldThrowException() throws Exception {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));
        when(storageService.downloadExplanatoryFile(1L, 1L, 1L, "draft.pdf")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.getItemFile(1L, 1L, 1L));

        assertEquals("File not found for item with id: 1", exception.getMessage());
        verify(explanatoryNoteRepository, times(1)).findById(1L);
        verify(storageService, times(1)).downloadExplanatoryFile(1L, 1L, 1L, "draft.pdf");
    }

    @Test
    void isItemBelongsToStudent_ShouldReturnTrue() {
        when(explanatoryNoteRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        boolean result = explanatoryNoteItemService.isItemBelongsToStudent(1L, 1L);

        assertTrue(result);
        verify(explanatoryNoteRepository, times(1)).existsByIdAndUserId(1L, 1L);
    }

    @Test
    void isItemBelongsToStudent_ShouldReturnFalse() {
        when(explanatoryNoteRepository.existsByIdAndUserId(1L, 999L)).thenReturn(false);

        boolean result = explanatoryNoteItemService.isItemBelongsToStudent(1L, 999L);

        assertFalse(result);
        verify(explanatoryNoteRepository, times(1)).existsByIdAndUserId(1L, 999L);
    }

    @Test
    void isItemBelongsToTeacher_ShouldReturnTrue() {
        when(explanatoryNoteRepository.existsByIdAndTeacherId(1L, 2L)).thenReturn(true);

        boolean result = explanatoryNoteItemService.isItemBelongsToTeacher(1L, 2L);

        assertTrue(result);
        verify(explanatoryNoteRepository, times(1)).existsByIdAndTeacherId(1L, 2L);
    }

    @Test
    void isItemBelongsToTeacher_ShouldReturnFalse() {
        when(explanatoryNoteRepository.existsByIdAndTeacherId(1L, 999L)).thenReturn(false);

        boolean result = explanatoryNoteItemService.isItemBelongsToTeacher(1L, 999L);

        assertFalse(result);
        verify(explanatoryNoteRepository, times(1)).existsByIdAndTeacherId(1L, 999L);
    }

    @Test
    void draftItem_WhenAllChaptersSubmitted_ShouldThrowException() throws Exception {
        workTemplate.setCountOfChapters(1);
        project.getItems().add(approvedItem);

        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.draftItem(1L, 1L, multipartFile));

        assertEquals("Project already has all items submitted", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void draftItem_WhenUserNotFound_ShouldThrowException() throws Exception {
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.draftItem(1L, 1L, multipartFile));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(projectRepository, never()).save(any(Project.class));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void submitItem_WhenUserNotFound_ShouldThrowException() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.submitItem(1L, 1L));

        assertEquals("User not found with id: 1", exception.getMessage());
        verify(explanatoryNoteRepository, never()).save(any(ExplanatoryNoteItem.class));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void approveItem_WhenUserNotFound_ShouldThrowException() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.approveItem(1L, 2L, "Comment"));

        assertEquals("User not found with id: 2", exception.getMessage());
        verify(explanatoryNoteRepository, never()).save(any(ExplanatoryNoteItem.class));
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void rejectItem_WhenUserNotFound_ShouldThrowException() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(draftItem));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> explanatoryNoteItemService.rejectItem(1L, 2L, "Comment"));

        assertEquals("User not found with id: 2", exception.getMessage());
        verify(explanatoryNoteRepository, never()).save(any(ExplanatoryNoteItem.class));
        verify(userRepository, times(1)).findById(2L);
    }
}
