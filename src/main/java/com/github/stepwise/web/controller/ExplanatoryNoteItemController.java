package com.github.stepwise.web.controller;

import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.ExplanatoryNoteItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/explanatory-note-item")
@Slf4j
@RequiredArgsConstructor
public class ExplanatoryNoteItemController {

  private final ExplanatoryNoteItemService explanatoryNoteItemService;

  @PostMapping(path = "/draft", consumes = "multipart/form-data")
  @PreAuthorize("hasRole('ROLE_STUDENT')")
  public ResponseEntity<Void> createExplanatoryNoteItem(@RequestPart("file") MultipartFile file,
      @RequestPart("projectId") String projectId, @AuthenticationPrincipal UserDetails userDetails)
      throws Exception {
    Long currentUserId = ((AppUserDetails) userDetails).getId();
    log.info("Creating explanatory note item with file: {}, userId: {}, porjectId: {}",
        file.getOriginalFilename(), currentUserId, projectId);

    explanatoryNoteItemService.draftItem(currentUserId, Long.valueOf(projectId), file);

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping("/file")
  @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
  public ResponseEntity<InputStreamResource> downloadItemFile(
      @AuthenticationPrincipal UserDetails userDetails, @RequestParam(required = false) Long userId,
      @RequestParam Long projectId, @RequestParam Long itemId) throws Exception {
    AppUserDetails appUserDetails = (AppUserDetails) userDetails;

    log.info("Downloading item file for userId: {}, projectId: {}, itemId: {}", userId, projectId,
        itemId);

    if (appUserDetails.getRole() == UserRole.STUDENT && appUserDetails.getId().equals(userId)) {
      log.warn("Unauthorized access attempt by userId: {}, projectId: {}, itemId: {}",
          appUserDetails.getId(), projectId, itemId);
      throw new IllegalArgumentException("Unauthorized access to item file");
    }

    Long ownerId = userId == null ? appUserDetails.getId() : userId;

    InputStream inputStream = explanatoryNoteItemService.getItemFile(ownerId, projectId, itemId);

    return new ResponseEntity<>(new InputStreamResource(inputStream), HttpStatus.OK);
  }



}
