package com.github.stepwise.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.github.stepwise.configuration.FileUploadConfig;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.repository.ExplanatoryNoteRepository;
import com.github.stepwise.repository.ProjectRepository;
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

  @Transactional
  public void draftItem(Long userId, Long projectId, MultipartFile file) throws Exception {
    log.info("Creating explanatory note item for userId: {}, projectId: {}, file: {}", userId,
        projectId, file.getOriginalFilename());

    if (!fileUploadConfig.getAllowedMimeTypes().contains(file.getContentType()))
      throw new IllegalArgumentException(
          "Only allowed file types are: " + fileUploadConfig.getAllowedMimeTypes());

    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + projectId));

    List<ExplanatoryNoteItem> items = project.getItems();

    if (project.getStudent().getId() != userId)
      throw new IllegalArgumentException(
          "User with id " + userId + " is not the owner of project with id: " + projectId);

    if (items.size() >= (int) project.getAcademicWork().getCountOfChapters())
      throw new IllegalArgumentException("Project already has all items submitted");

    if (items.size() > 0 && (items.getLast().getStatus() == ItemStatus.SUBMITTED
        || items.getLast().getStatus() == ItemStatus.DRAFT))
      throw new IllegalArgumentException("Cannot submit or draft more than one item at a time");

    ExplanatoryNoteItem newItem = new ExplanatoryNoteItem(items.size(), ItemStatus.DRAFT,
        file.getOriginalFilename(), LocalDateTime.now(), project);

    items.add(newItem);

    Project savedProject = projectRepository.save(project);

    Long newExplanatoryNoteItemId = savedProject.getItems().stream()
        .max(Comparator.comparingInt(ExplanatoryNoteItem::getOrderNumber))
        .orElseThrow(() -> new RuntimeException("Failed to define last item ID")).getId();

    log.info("Explanatory note item created successfully for projectId: {}, itemId: {}",
        savedProject.getId(), newItem.getId());

    storageService.uploadExplanatoryFile(userId, projectId, newExplanatoryNoteItemId, file);
  }

  public void submitItem(Long itemId) {
    log.info("Submitting explanatory note item with id: {}", itemId);

    ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
        () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

    if (item.getStatus() != ItemStatus.DRAFT) {
      log.error("Item with id: {} is not in DRAFT status, current status: {}", itemId,
          item.getStatus());
      throw new IllegalArgumentException("Item with id: " + itemId + " is not in DRAFT status");
    }

    item.setStatus(ItemStatus.SUBMITTED);
    item.setSubmittedAt(LocalDateTime.now());

    explanatoryNoteRepository.save(item);

    log.info("Explanatory note item with id: {} submitted successfully", itemId);
  }

  public void approveItem(Long itemId) {
    log.info("Approving explanatory note item with id: {}", itemId);

    ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
        () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

    if (item.getStatus() != ItemStatus.SUBMITTED) {
      log.error("Item with id: {} is not in SUBMITTED status, current status: {}", itemId,
          item.getStatus());
      throw new IllegalArgumentException("Item with id: " + itemId + " is not in SUBMITTED status");
    }

    item.setStatus(ItemStatus.APPROVED);
    item.setApprovedAt(LocalDateTime.now());

    explanatoryNoteRepository.save(item);

    log.info("Explanatory note item with id: {} approved successfully", itemId);
  }

  public void rejectItem(Long itemId) {
    log.info("Rejecting explanatory note item with id: {}", itemId);

    ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
        () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

    if (item.getStatus() != ItemStatus.SUBMITTED) {
      log.error("Item with id: {} is not in SUBMITTED status, current status: {}", itemId,
          item.getStatus());
      throw new IllegalArgumentException("Item with id: " + itemId + " is not in SUBMITTED status");
    }

    item.setStatus(ItemStatus.REJECTED);
    item.setRejectedAt(LocalDateTime.now());

    explanatoryNoteRepository.save(item);

    log.info("Explanatory note item with id: {} rejected successfully", itemId);
  }

  public InputStream getItemFile(Long userId, Long projectId, Long itemId) throws Exception {
    ExplanatoryNoteItem item = explanatoryNoteRepository.findById(itemId).orElseThrow(
        () -> new IllegalArgumentException("Explanatory note item not found with id: " + itemId));

    InputStream inputStream =
        storageService.downloadExplanatoryFile(userId, projectId, itemId, item.getFileName());

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
