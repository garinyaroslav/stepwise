package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
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
import com.github.stepwise.exception.NotFoundException;
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

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).role(UserRole.STUDENT).build();
        teacher = User.builder().id(2L).role(UserRole.TEACHER).build();

        WorkTemplate workTemplate = WorkTemplate.builder().countOfChapters(3).build();
        AcademicWork academicWork = AcademicWork.builder().workTemplate(workTemplate).build();

        project = Project.builder()
                .id(1L)
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

        file = mock(MultipartFile.class);
    }

    @Test
    void draftItem_ShouldCreateNewItem_WhenNoItemsExist() throws Exception {
        project.getItems().clear();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(file.getOriginalFilename()).thenReturn("document.pdf");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            if (!saved.getItems().isEmpty()) {
                ExplanatoryNoteItem lastItem = saved.getItems().getLast();
                if (lastItem.getId() == null) {
                    lastItem.setId(10L);
                }
                if (!lastItem.getHistory().isEmpty()) {
                    ItemHistory lastHistory = lastItem.getHistory().getLast();
                    if (lastHistory.getId() == null) {
                        lastHistory.setId(100L);
                    }
                }
            }
            return saved;
        });

        service.draftItem(1L, 1L, file);

        verify(projectRepository, times(2)).save(any(Project.class));
        verify(storageService).uploadExplanatoryFile(eq(1L), eq(1L), eq(10L), anyLong(), eq(file));
    }

    @Test
    void draftItem_ShouldUpdateExistingDraft() throws Exception {
        project.getItems().clear();
        project.getItems().add(item);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(file.getOriginalFilename()).thenReturn("updated.pdf");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            if (!saved.getItems().isEmpty()) {
                ExplanatoryNoteItem lastItem = saved.getItems().getLast();
                if (!lastItem.getHistory().isEmpty()) {
                    ItemHistory lastHistory = lastItem.getHistory().getLast();
                    if (lastHistory.getId() == null) {
                        lastHistory.setId(200L);
                    }
                }
            }
            return saved;
        });

        service.draftItem(1L, 1L, file);

        verify(storageService).uploadExplanatoryFile(eq(1L), eq(1L), eq(1L), anyLong(), eq(file));
        assertEquals(ItemStatus.DRAFT, item.getStatus());
    }

    @Test
    void draftItem_ShouldReuseRejectedItem() throws Exception {
        item.setStatus(ItemStatus.REJECTED);
        project.getItems().clear();
        project.getItems().add(item);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));
        when(file.getOriginalFilename()).thenReturn("new-version.pdf");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project saved = invocation.getArgument(0);
            if (!saved.getItems().isEmpty()) {
                ExplanatoryNoteItem lastItem = saved.getItems().getLast();
                if (!lastItem.getHistory().isEmpty()) {
                    ItemHistory lastHistory = lastItem.getHistory().getLast();
                    if (lastHistory.getId() == null) {
                        lastHistory.setId(200L);
                    }
                }
            }
            return saved;
        });

        service.draftItem(1L, 1L, file);

        verify(storageService).uploadExplanatoryFile(eq(1L), eq(1L), eq(1L), anyLong(), eq(file));
        assertEquals(ItemStatus.DRAFT, item.getStatus());
    }

    @Test
    void draftItem_ShouldThrow_WhenFileTypeNotAllowed() {
        when(file.getContentType()).thenReturn("text/plain");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));

        assertThrows(IllegalArgumentException.class, () -> service.draftItem(1L, 1L, file));
    }

    @Test
    void draftItem_ShouldThrow_WhenUserNotOwner() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));

        assertThrows(AccessDeniedException.class, () -> service.draftItem(999L, 1L, file));
    }

    @Test
    void draftItem_ShouldThrow_WhenAllItemsApproved() {
        project.getItems().clear();
        for (int i = 0; i < 3; i++) {
            project.getItems().add(ExplanatoryNoteItem.builder()
                    .id((long) i)
                    .orderNumber(i)
                    .status(ItemStatus.APPROVED)
                    .build());
        }

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(file.getContentType()).thenReturn("application/pdf");
        when(fileUploadConfig.getAllowedMimeTypes()).thenReturn(List.of("application/pdf"));

        assertThrows(IllegalArgumentException.class, () -> service.draftItem(1L, 1L, file));
    }

    @Test
    void submitItem_ShouldChangeStatusToSubmitted() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(explanatoryNoteRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        service.submitItem(1L, 1L);

        assertEquals(ItemStatus.SUBMITTED, item.getStatus());
        assertFalse(item.getHistory().isEmpty());
        verify(explanatoryNoteRepository).save(item);
    }

    @Test
    void submitItem_ShouldThrow_WhenNotDraft() {
        item.setStatus(ItemStatus.SUBMITTED);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(explanatoryNoteRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThrows(IllegalArgumentException.class, () -> service.submitItem(1L, 1L));
    }

    @Test
    void approveItem_ShouldChangeStatusToApproved() {
        item.setStatus(ItemStatus.SUBMITTED);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(explanatoryNoteRepository.existsByIdAndTeacherId(1L, 2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        service.approveItem(1L, 2L, "Хорошо");

        assertEquals(ItemStatus.APPROVED, item.getStatus());
        verify(explanatoryNoteRepository).save(item);
    }

    @Test
    void rejectItem_ShouldChangeStatusToRejected() {
        item.setStatus(ItemStatus.SUBMITTED);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(explanatoryNoteRepository.existsByIdAndTeacherId(1L, 2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        service.rejectItem(1L, 2L, "Исправить");

        assertEquals(ItemStatus.REJECTED, item.getStatus());
        verify(explanatoryNoteRepository).save(item);
    }

    @Test
    void rejectItem_ShouldThrow_WhenNotSubmitted() {
        item.setStatus(ItemStatus.DRAFT);
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));
        when(explanatoryNoteRepository.existsByIdAndTeacherId(1L, 2L)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(teacher));

        assertThrows(IllegalArgumentException.class, () -> service.rejectItem(1L, 2L, "Коммент"));
    }

    @Test
    void getItemFile_ShouldReturnLatestFile() throws Exception {
        ItemHistory history = ItemHistory.builder()
                .id(10L)
                .fileName("test.pdf")
                .build();
        item.getHistory().add(history);

        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));

        InputStream stream = new ByteArrayInputStream(new byte[0]);
        when(storageService.downloadExplanatoryFile(anyLong(), anyLong(), anyLong(), eq(10L), eq("test.pdf")))
                .thenReturn(stream);

        assertNotNull(service.getItemFile(1L, 1L, 1L, null));
    }

    @Test
    void getItemFile_ShouldThrow_WhenNoFile() {
        when(explanatoryNoteRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> service.getItemFile(1L, 1L, 1L, null));
    }

    @Test
    void isItemBelongsToStudent() {
        when(explanatoryNoteRepository.existsByIdAndUserId(1L, 1L)).thenReturn(true);
        assertTrue(service.isItemBelongsToStudent(1L, 1L));
    }

    @Test
    void resolveAccessibleUserId_ForStudent() {
        assertEquals(1L, service.resolveAccessibleUserId(null, 1L, UserRole.STUDENT));
        assertThrows(AccessDeniedException.class,
                () -> service.resolveAccessibleUserId(2L, 1L, UserRole.STUDENT));
    }

}
