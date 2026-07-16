package com.github.stepwise.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.stepwise.configuration.FileUploadConfig;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemHistory;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.ExplanatoryNoteRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExplanatoryNoteItemService {

    private final ProjectRepository projectRepository;

    private final ExplanatoryNoteRepository explanatoryNoteRepository;

    private final FileUploadConfig fileUploadConfig;

    private final StorageService storageService;

    private final UserRepository userRepository;

    @Transactional
    public void draftItem(Long userId, Long projectId, MultipartFile file) throws Exception {
        log.info("Creating explanatory note item for userId: {}, projectId: {}, file: {}",
                userId, projectId, file.getOriginalFilename());

        if (!fileUploadConfig.getAllowedMimeTypes().contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Only allowed file types are: " + fileUploadConfig.getAllowedMimeTypes());
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));

        if (!project.getStudent().getId().equals(userId)) {
            throw new AccessDeniedException(
                    "User with id " + userId + " is not the owner of project with id: " + projectId);
        }

        List<ExplanatoryNoteItem> items = project.getItems();
        items.sort(Comparator.comparing(ExplanatoryNoteItem::getOrderNumber));

        if (!items.isEmpty()) {
            if (items.size() >= project.getAcademicWork().getWorkTemplate().getCountOfChapters()
                    && items.getLast().getStatus() == ItemStatus.APPROVED) {
                throw new IllegalArgumentException("Project already has all items submitted");
            }
            if (items.getLast().getStatus() == ItemStatus.SUBMITTED) {
                throw new IllegalArgumentException("Cannot submit more than one item at a time");
            }
        }

        User changedBy = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        ExplanatoryNoteItem item = resolveDraftItem(items, project, projectId);

        Project savedProject = projectRepository.save(project);
        item = savedProject.getItems().getLast();

        ItemHistory historyEntry = ItemHistory.builder()
                .item(item)
                .previousStatus(item.getHistory().isEmpty() ? null : item.getHistory().getLast().getNewStatus())
                .newStatus(ItemStatus.DRAFT)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .fileName(file.getOriginalFilename())
                .build();

        item.getHistory().add(historyEntry);
        projectRepository.save(savedProject);

        Long historyId = item.getHistory().getLast().getId();
        storageService.uploadExplanatoryFile(userId, projectId, item.getId(), historyId, file);

        log.info("Draft created for projectId: {}, itemId: {}, historyId: {}", projectId, item.getId(), historyId);
    }

    private ExplanatoryNoteItem resolveDraftItem(List<ExplanatoryNoteItem> items, Project project, Long projectId) {
        if (items.isEmpty() || items.getLast().getStatus() == ItemStatus.APPROVED) {
            ExplanatoryNoteItem item = new ExplanatoryNoteItem(items.size(), ItemStatus.DRAFT, project);
            items.add(item);
            return item;
        }
        if (items.getLast().getStatus() == ItemStatus.DRAFT || items.getLast().getStatus() == ItemStatus.REJECTED) {
            ExplanatoryNoteItem item = items.getLast();
            item.setStatus(ItemStatus.DRAFT);
            return item;
        }
        throw new IllegalArgumentException("Cannot draft item for project: " + projectId);
    }

    public void submitItem(Long itemId, Long studentId) {
        log.info("Submitting explanatory note item with id: {}", itemId);
        assertBelongsToStudent(itemId, studentId);

        ExplanatoryNoteItem item = getItemOrThrow(itemId);
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + studentId));

        assertStatus(item, ItemStatus.DRAFT);

        addHistoryEntry(item, ItemStatus.SUBMITTED, student, null);
        item.setStatus(ItemStatus.SUBMITTED);
        explanatoryNoteRepository.save(item);

        log.info("Explanatory note item with id: {} submitted successfully", itemId);
    }

    public void approveItem(Long itemId, Long teacherId, String teacherComment) {
        log.info("Approving explanatory note item with id: {}", itemId);
        assertBelongsToTeacher(itemId, teacherId);

        ExplanatoryNoteItem item = getItemOrThrow(itemId);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + teacherId));

        assertStatus(item, ItemStatus.SUBMITTED);

        addHistoryEntry(item, ItemStatus.APPROVED, teacher, teacherComment);
        item.setStatus(ItemStatus.APPROVED);
        explanatoryNoteRepository.save(item);

        log.info("Explanatory note item with id: {} approved successfully", itemId);
    }

    public void rejectItem(Long itemId, Long teacherId, String teacherComment) {
        log.info("Rejecting explanatory note item with id: {}", itemId);
        assertBelongsToTeacher(itemId, teacherId);

        ExplanatoryNoteItem item = getItemOrThrow(itemId);
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + teacherId));

        assertStatus(item, ItemStatus.SUBMITTED);

        addHistoryEntry(item, ItemStatus.REJECTED, teacher, teacherComment);
        item.setStatus(ItemStatus.REJECTED);
        explanatoryNoteRepository.save(item);

        log.info("Explanatory note item with id: {} rejected successfully", itemId);
    }

    public String getItemFileName(Long itemId, Long historyId) {
        ExplanatoryNoteItem item = getItemOrThrow(itemId);

        if (historyId != null) {
            return item.getHistory().stream()
                    .filter(h -> h.getId().equals(historyId) && h.getFileName() != null)
                    .findFirst()
                    .map(ItemHistory::getFileName)
                    .orElseThrow(() -> new NotFoundException("History not found: " + historyId));
        }
        return item.getHistory().stream()
                .filter(h -> h.getFileName() != null)
                .reduce((first, second) -> second)
                .map(ItemHistory::getFileName)
                .orElseThrow(() -> new NotFoundException("No file for item: " + itemId));
    }

    public InputStream getItemFile(Long userId, Long projectId, Long itemId, Long historyId) throws Exception {
        ExplanatoryNoteItem item = getItemOrThrow(itemId);

        ItemHistory targetHistory = historyId != null
                ? item.getHistory().stream()
                        .filter(h -> h.getId().equals(historyId) && h.getFileName() != null)
                        .findFirst()
                        .orElseThrow(() -> new NotFoundException("History entry not found: " + historyId))
                : item.getHistory().stream()
                        .filter(h -> h.getFileName() != null)
                        .reduce((first, second) -> second)
                        .orElseThrow(() -> new NotFoundException("No file found for item: " + itemId));

        return storageService.downloadExplanatoryFile(
                userId, projectId, itemId, targetHistory.getId(), targetHistory.getFileName());
    }

    public boolean isItemBelongsToStudent(Long itemId, Long studentId) {
        return explanatoryNoteRepository.existsByIdAndUserId(itemId, studentId);
    }

    public boolean isItemBelongsToTeacher(Long itemId, Long teacherId) {
        return explanatoryNoteRepository.existsByIdAndTeacherId(itemId, teacherId);
    }

    public Long resolveAccessibleUserId(Long requestedUserId, Long principalId, UserRole principalRole) {
        Long targetUserId = requestedUserId == null ? principalId : requestedUserId;

        if (principalRole == UserRole.STUDENT && !principalId.equals(targetUserId)) {
            log.warn("Student {} attempted to access resource of user {}", principalId, targetUserId);
            throw new AccessDeniedException("Students can only access their own resources");
        }

        return targetUserId;
    }

    private void assertBelongsToStudent(Long itemId, Long studentId) {
        if (!isItemBelongsToStudent(itemId, studentId)) {
            log.warn("Item {} does not belong to student {}", itemId, studentId);
            throw new NotFoundException("Explanatory note item not found with id: " + itemId);
        }
    }

    private void assertBelongsToTeacher(Long itemId, Long teacherId) {
        if (!isItemBelongsToTeacher(itemId, teacherId)) {
            log.warn("Item {} does not belong to teacher {}", itemId, teacherId);
            throw new NotFoundException("Explanatory note item not found with id: " + itemId);
        }
    }

    private void assertStatus(ExplanatoryNoteItem item, ItemStatus expected) {
        if (item.getStatus() != expected) {
            log.error("Item with id: {} is not in {} status, current status: {}", item.getId(), expected,
                    item.getStatus());
            throw new IllegalArgumentException("Item with id: " + item.getId() + " is not in " + expected + " status");
        }
    }

    private void addHistoryEntry(ExplanatoryNoteItem item, ItemStatus newStatus, User changedBy,
            String teacherComment) {
        ItemHistory entry = ItemHistory.builder()
                .item(item)
                .previousStatus(item.getStatus())
                .newStatus(newStatus)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .teacherComment(teacherComment)
                .build();
        item.getHistory().add(entry);
    }

    private ExplanatoryNoteItem getItemOrThrow(Long itemId) {
        return explanatoryNoteRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Explanatory note item not found with id: " + itemId));
    }

}
