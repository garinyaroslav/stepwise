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

    if (items.size() > 0 && items.getLast().getStatus() == ItemStatus.SUBMITTED)
      throw new IllegalArgumentException("Cannot submit more than one item at a time");

    ExplanatoryNoteItem newItem = new ExplanatoryNoteItem(items.size() + 1, ItemStatus.DRAFT,
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



}
