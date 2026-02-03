package com.github.stepwise.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.github.stepwise.configuration.FileUploadConfig;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemHistory;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.User;
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
        log.info("Creating explanatory note item for userId: {}, projectId: {}, file: {}", userId,
                projectId, file.getOriginalFilename());

        if (!fileUploadConfig.getAllowedMimeTypes().contains(file.getContentType()))
            throw new IllegalArgumentException(
                    "Only allowed file types are: " + fileUploadConfig.getAllowedMimeTypes());

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

        if (!project.getStudent().getId().equals(userId))
            throw new IllegalArgumentException(
                    "User with id " + userId + " is not the owner of project with id: " + projectId);

        List<ExplanatoryNoteItem> items = project.getItems();

        if (!items.isEmpty()) {
            if (items.size() >= (int) project.getAcademicWork().getWorkTemplate().getCountOfChapters()
                    && (items.getLast().getStatus() == ItemStatus.SUBMITTED
                            || items.getLast().getStatus() == ItemStatus.APPROVED))
                throw new IllegalArgumentException("Project already has all items submitted");

            if (items.getLast().getStatus() == ItemStatus.SUBMITTED)
                throw new IllegalArgumentException("Cannot submit more than one item at a time");
        }

        ExplanatoryNoteItem newItem;

        if (items.isEmpty() || items.getLast().getStatus() == ItemStatus.APPROVED) {
            newItem = new ExplanatoryNoteItem(items.size(), ItemStatus.DRAFT, file.getOriginalFilename(), project);

            User changedBy = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

            ItemHistory newItemHistory = ItemHistory.builder().item(newItem)
                    .previousStatus(null)
                    .newStatus(ItemStatus.DRAFT)
                    .changedAt(LocalDateTime.now())
                    .changedBy(changedBy)
                    .build();

            newItem.getHistory().add(newItemHistory);

            items.add(newItem);
        } else if (items.getLast().getStatus() == ItemStatus.DRAFT
                || items.getLast().getStatus() == ItemStatus.REJECTED) {
            storageService.deleteExplanatoryFile(userId, projectId,
                    items.getLast().getId(), items.getLast().getFileName());

            newItem = items.getLast();

            User changedBy = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

            ItemHistory newItemHistory = ItemHistory.builder().item(newItem)
                    .previousStatus(items.getLast().getStatus())
                    .newStatus(ItemStatus.DRAFT)
                    .changedAt(LocalDateTime.now())
                    .changedBy(changedBy)
                    .build();

            newItem.getHistory().add(newItemHistory);
            newItem.setStatus(ItemStatus.DRAFT);
            newItem.setFileName(file.getOriginalFilename());
        } else
            throw new IllegalArgumentException(
                    "Cannot create draft item for project with id: " + projectId);

        Project savedProject = projectRepository.save(project);

        log.info("Explanatory note item created successfully for projectId: {}, itemId: {}",
                savedProject.getId(), newItem.getId());

        storageService.uploadExplanatoryFile(userId, projectId, newItem.getId(), file);
    }

    public void submitItem(Long itemId, Long studentId) {
        log.info("Submitting explanatory note item with id: {}", itemId);

        ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
                () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + studentId));

        if (item.getStatus() != ItemStatus.DRAFT) {
            log.error("Item with id: {} is not in DRAFT status, current status: {}", itemId,
                    item.getStatus());
            throw new IllegalArgumentException("Item with id: " + itemId + " is not in DRAFT status");
        }

        ItemHistory newItemHistory = ItemHistory.builder().item(item)
                .previousStatus(item.getStatus())
                .newStatus(ItemStatus.SUBMITTED)
                .changedAt(LocalDateTime.now())
                .changedBy(student)
                .build();

        item.getHistory().add(newItemHistory);
        item.setStatus(ItemStatus.SUBMITTED);

        explanatoryNoteRepository.save(item);

        log.info("Explanatory note item with id: {} submitted successfully", itemId);
    }

    public void approveItem(Long itemId, Long teacherId, String teacherComment) {
        log.info("Approving explanatory note item with id: {}", itemId);

        ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
                () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + teacherId));

        if (item.getStatus() != ItemStatus.SUBMITTED) {
            log.error("Item with id: {} is not in SUBMITTED status, current status: {}", itemId,
                    item.getStatus());
            throw new IllegalArgumentException("Item with id: " + itemId + " is not in SUBMITTED status");
        }

        ItemHistory newItemHistory = ItemHistory.builder().item(item)
                .previousStatus(item.getStatus())
                .newStatus(ItemStatus.APPROVED)
                .changedAt(LocalDateTime.now())
                .changedBy(teacher)
                .teacherComment(teacherComment)
                .build();

        item.getHistory().add(newItemHistory);
        item.setStatus(ItemStatus.APPROVED);

        explanatoryNoteRepository.save(item);

        log.info("Explanatory note item with id: {} approved successfully", itemId);
    }

    public void rejectItem(Long itemId, Long teacherId, String teacherComment) {
        log.info("Rejecting explanatory note item with id: {}", itemId);

        ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
                () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + teacherId));

        if (item.getStatus() != ItemStatus.SUBMITTED) {
            log.error("Item with id: {} is not in SUBMITTED status, current status: {}", itemId,
                    item.getStatus());
            throw new IllegalArgumentException("Item with id: " + itemId + " is not in SUBMITTED status");
        }

        ItemHistory newItemHistory = ItemHistory.builder().item(item)
                .previousStatus(item.getStatus())
                .newStatus(ItemStatus.REJECTED)
                .changedAt(LocalDateTime.now())
                .changedBy(teacher)
                .teacherComment(teacherComment)
                .build();

        item.getHistory().add(newItemHistory);
        item.setStatus(ItemStatus.REJECTED);

        explanatoryNoteRepository.save(item);

        log.info("Explanatory note item with id: {} rejected successfully", itemId);
    }

    public InputStream getItemFile(Long userId, Long projectId, Long itemId) throws Exception {
        ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
                () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

        InputStream inputStream = storageService.downloadExplanatoryFile(userId, projectId, itemId, item.getFileName());

        if (inputStream == null) {
            log.error("File not found for userId: {}, projectId; {}, itemId: {}", userId, projectId,
                    itemId);
            throw new IllegalArgumentException("File not found for item with id: " + itemId);
        }

        return inputStream;
    }

    public boolean isItemBelongsToStudent(Long itemId, Long studentId) {
        log.info("Checking if item with id: {} belongs to student with id: {}", itemId, studentId);

        return explanatoryNoteRepository.existsByIdAndUserId(itemId, studentId);
    }

    public boolean isItemBelongsToTeacher(Long itemId, Long teacherId) {
        log.info("Checking if item with id: {}, belongs to teacher with id: {}", itemId, teacherId);

        return explanatoryNoteRepository.existsByIdAndTeacherId(itemId, teacherId);
    }

}
