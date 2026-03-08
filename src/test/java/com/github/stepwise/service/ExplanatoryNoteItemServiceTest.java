package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
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
import com.github.stepwise.entity.*;
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
    private ExplanatoryNoteItemService service;

    private User student;
    private User teacher;
    private Project project;
    private ExplanatoryNoteItem item;
    private MultipartFile file;
    private AcademicWork academicWork;
    private WorkTemplate workTemplate;

    @BeforeEach
    void setUp() {
        student = User.builder()
                .id(1L)
                .username("student1")
                .email("student@test.com")
                .role(UserRole.STUDENT)
                .build();

        teacher = User.builder()
                .id(2L)
                .username("teacher1")
                .email("teacher@test.com")
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
                .title("Test Project")
                .description("Test Description")
                .student(student)
                .academicWork(academicWork)
                .items(new ArrayList<>())
                .build();

        item = ExplanatoryNoteItem.builder()
                .id(1L)
                .orderNumber(0)
                .status(ItemStatus.DRAFT)
                .project(project)
                .history(new ArrayList<>())
                .build();

        project.getItems().add(item);

        file = mock(MultipartFile.class);
    }

    @Test
    void draftItem_ShouldCreateNewItem_WhenNoItemsExist() throws Exception {
        project.setItems(new ArrayList<>());

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        Project savedProject = Project.builder()
                .id(1L)
                .title("Test Project")
                .description("Test Description")
                .student(student)
                .academicWork(academicWork)
                .items(new ArrayList<>())
                .build();

        ItemHistory savedHistory = ItemHistory.builder()
                .id(100L)
                .newStatus(ItemStatus.DRAFT)
                .changedAt(LocalDateTime.now())
                .fileName("document.pdf")
                .build();

        ExplanatoryNoteItem savedItem = ExplanatoryNoteItem.builder()
                .id(10L)
                .orderNumber(0)
                .status(ItemStatus.DRAFT)
                .project(savedProject)
                .history(new ArrayList<>(List.of(savedHistory)))
                .build();
        savedHistory.setItem(savedItem);
        savedProject.getItems().add(savedItem);

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        service.draftItem(1L, 1L, file);

        verify(projectRepository, times(2)).save(any(Project.class));
        verify(storageService).uploadExplanatoryFile(eq(1L), eq(1L), anyLong(), isNull(), eq(file));
    }

    @Test
    void draftItem_ShouldThrowException_WhenFileTypeNotAllowed() {
        when(file.getContentType()).thenReturn("text/plain");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));

        assertThrows(IllegalArgumentException.class, () -> service.draftItem(1L, 1L, file));
    }

    @Test
    void draftItem_ShouldThrowException_WhenUserNotOwner() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));

        assertThrows(IllegalArgumentException.class, () -> service.draftItem(999L, 1L, file));
    }

    @Test
    void submitItem_ShouldChangeStatusToSubmitted() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        service.submitItem(1L, 1L);

        assertEquals(ItemStatus.SUBMITTED, item.getStatus());
        assertFalse(item.getHistory().isEmpty());
        verify(explanatoryNoteRepository).save(item);
    }

    @Test
    void submitItem_ShouldThrowException_WhenItemNotInDraft() {
        item.setStatus(ItemStatus.SUBMITTED);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> service.submitItem(1L, 1L));
    }

    @Test
    void approveItem_ShouldChangeStatusToApproved_WithComment() {
        item.setStatus(ItemStatus.SUBMITTED);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));
        String comment = "Хорошая работа";

        service.approveItem(1L, 2L, comment);

        assertEquals(ItemStatus.APPROVED, item.getStatus());
        assertTrue(item.getHistory().stream()
                .anyMatch(h -> h.getTeacherComment() != null && h.getTeacherComment().equals(comment)));
        verify(explanatoryNoteRepository).save(item);
    }

    @Test
    void rejectItem_ShouldChangeStatusToRejected() {
        item.setStatus(ItemStatus.SUBMITTED);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        service.rejectItem(1L, 2L, "Нужны исправления");

        assertEquals(ItemStatus.REJECTED, item.getStatus());
        verify(explanatoryNoteRepository).save(item);
    }

    @Test
    void rejectItem_ShouldThrowException_WhenItemNotSubmitted() {
        item.setStatus(ItemStatus.DRAFT);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.rejectItem(1L, 2L, "Комментарий"));

        assertTrue(exception.getMessage().contains("is not in SUBMITTED status"));
    }

    @Test
    void getItemFile_ShouldReturnLatestFileFromHistory() throws Exception {
        ItemHistory historyWithFile = ItemHistory.builder()
                .id(10L)
                .newStatus(ItemStatus.DRAFT)
                .changedAt(LocalDateTime.now())
                .fileName("test.pdf")
                .item(item)
                .build();
        item.getHistory().add(historyWithFile);

        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        InputStream mockStream = new ByteArrayInputStream("test content".getBytes());
        when(storageService.downloadExplanatoryFile(1L, 1L, 1L, 10L, "test.pdf"))
                .thenReturn(mockStream);

        InputStream result = service.getItemFile(1L, 1L, 1L, null);

        assertNotNull(result);
        assertEquals(mockStream, result);
    }

    @Test
    void getItemFile_ShouldReturnSpecificHistoryFile() throws Exception {
        ItemHistory history1 = ItemHistory.builder()
                .id(10L)
                .newStatus(ItemStatus.DRAFT)
                .changedAt(LocalDateTime.now().minusDays(1))
                .fileName("v1.pdf")
                .item(item)
                .build();
        ItemHistory history2 = ItemHistory.builder()
                .id(11L)
                .newStatus(ItemStatus.DRAFT)
                .changedAt(LocalDateTime.now())
                .fileName("v2.pdf")
                .item(item)
                .build();
        item.getHistory().addAll(List.of(history1, history2));

        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        InputStream mockStream = new ByteArrayInputStream("v1 content".getBytes());
        when(storageService.downloadExplanatoryFile(1L, 1L, 1L, 10L, "v1.pdf"))
                .thenReturn(mockStream);

        InputStream result = service.getItemFile(1L, 1L, 1L, 10L);

        assertNotNull(result);
        assertEquals(mockStream, result);
    }

    @Test
    void getItemFile_ShouldThrowException_WhenNoFileInHistory() throws Exception {
        ItemHistory historyNoFile = ItemHistory.builder()
                .id(20L)
                .newStatus(ItemStatus.SUBMITTED)
                .changedAt(LocalDateTime.now())
                .item(item)
                .build();
        item.getHistory().add(historyNoFile);

        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(IllegalArgumentException.class,
                () -> service.getItemFile(1L, 1L, 1L, null));
    }

    @Test
    void isItemBelongsToStudent_ShouldReturnTrue_WhenItemBelongsToStudent() {
        when(explanatoryNoteRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);

        assertTrue(service.isItemBelongsToStudent(1L, 1L));
    }

    @Test
    void isItemBelongsToStudent_ShouldReturnFalse_WhenItemDoesNotBelongToStudent() {
        when(explanatoryNoteRepository.existsByIdAndUserId(1L, 1L)).thenReturn(false);

        assertFalse(service.isItemBelongsToStudent(1L, 1L));
    }

    @Test
    void isItemBelongsToTeacher_ShouldReturnTrue_WhenTeacherHasAccess() {
        when(explanatoryNoteRepository.existsByIdAndTeacherId(1L, 2L)).thenReturn(true);

        assertTrue(service.isItemBelongsToTeacher(1L, 2L));
    }

    @Test
    void draftItem_ShouldUpdateExistingDraft_WhenLastItemIsDraft() throws Exception {
        ItemHistory existingHistory = ItemHistory.builder()
                .id(5L)
                .newStatus(ItemStatus.DRAFT)
                .changedAt(LocalDateTime.now().minusHours(1))
                .fileName("old.pdf")
                .item(item)
                .build();
        item.getHistory().add(existingHistory);

        project.getItems().clear();
        project.getItems().add(item);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(file.getOriginalFilename()).thenReturn("updated.pdf");
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        service.draftItem(1L, 1L, file);

        verify(storageService).uploadExplanatoryFile(eq(1L), eq(1L), anyLong(), isNull(), eq(file));
        ItemHistory lastHistory = item.getHistory().getLast();
        assertEquals("updated.pdf", lastHistory.getFileName());
    }

    @Test
    void draftItem_ShouldThrowException_WhenAllItemsSubmitted() {
        project.getItems().clear();

        for (int i = 0; i < 3; i++) {
            ExplanatoryNoteItem approvedItem = ExplanatoryNoteItem.builder()
                    .id((long) (i + 1))
                    .orderNumber(i)
                    .status(ItemStatus.APPROVED)
                    .project(project)
                    .history(new ArrayList<>())
                    .build();
            project.getItems().add(approvedItem);
        }

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));

        assertThrows(IllegalArgumentException.class, () -> service.draftItem(1L, 1L, file));
    }
}
