package com.github.stepwise.web.controller;

import java.io.InputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.ExplanatoryNoteItemService;
import com.github.stepwise.web.dto.RejectItemDto;
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
    public ResponseEntity<Void> createExplanatoryNoteItem(@RequestPart("projectId") String projectId,
            @RequestPart("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails)
            throws Exception {
        Long currentUserId = ((AppUserDetails) userDetails).getId();
        log.info("Creating explanatory note item with file: {}, userId: {}, porjectId: {}",
                file.getOriginalFilename(), currentUserId, projectId);

        explanatoryNoteItemService.draftItem(currentUserId, Long.valueOf(projectId), file);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/submit/{id}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<Void> submitExplanatoryNoteItem(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        Long currentStudentId = ((AppUserDetails) userDetails).getId();

        log.info("Submitting explanatory note item with id: {}, userId: {}", id, currentStudentId);

        if (!explanatoryNoteItemService.isItemBelongsToStudent(id, currentStudentId)) {
            log.error("Unauthorized access attempt by userId: {}, itemId: {}", currentStudentId, id);
            throw new IllegalArgumentException(
                    "Unauthorized access to item submission by userId: " + currentStudentId);
        }

        explanatoryNoteItemService.submitItem(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<Void> approveExplanatoryNoteItem(@PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        Long currentTeacherId = ((AppUserDetails) userDetails).getId();

        log.info("Submitting explanatory note item with id: {}, userId: {}", id, currentTeacherId);

        if (!explanatoryNoteItemService.isItemBelongsToTeacher(id, currentTeacherId)) {
            log.error("Cannot approve item with id: {}, by teacher with id: {}", id, currentTeacherId);
            throw new IllegalArgumentException(
                    "Cannot approve item with id: " + id + " by teacher with id: " + currentTeacherId);
        }

        explanatoryNoteItemService.approveItem(id);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/reject/{id}")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<Void> rejectExplanatoryNoteItem(@PathVariable Long id,
            @RequestBody RejectItemDto rejectItemDto, @AuthenticationPrincipal UserDetails userDetails)
            throws Exception {
        Long currentTeacherId = ((AppUserDetails) userDetails).getId();

        log.info("Rejecting explanatory note item with id: {}, userId: {}", id, currentTeacherId);

        if (!explanatoryNoteItemService.isItemBelongsToTeacher(id, currentTeacherId)) {
            log.error("Cannot reject item with id: {}, by teacher with id: {}", id, currentTeacherId);
            throw new IllegalArgumentException(
                    "Cannot reject item with id: " + id + " by teacher with id: " + currentTeacherId);
        }

        explanatoryNoteItemService.rejectItem(id, rejectItemDto.getTeacherComment());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/file")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<InputStreamResource> downloadItemFile(
            @AuthenticationPrincipal UserDetails userDetails, @RequestParam(required = false) Long userId,
            @RequestParam Long projectId, @RequestParam Long itemId) throws Exception {
        AppUserDetails appUserDetails = (AppUserDetails) userDetails;

        Long targetUserId = userId == null ? appUserDetails.getId() : userId;

        log.info("Downloading item file for userId: {}, projectId: {}, itemId: {}", targetUserId, projectId,
                itemId);

        if (appUserDetails.getRole() == UserRole.STUDENT && !appUserDetails.getId().equals(targetUserId)) {
            log.warn("Unauthorized access attempt by userId: {}, projectId: {}, itemId: {}",
                    appUserDetails.getId(), projectId, itemId);
            throw new IllegalArgumentException("Unauthorized access to item file");
        }

        InputStream inputStream = explanatoryNoteItemService.getItemFile(targetUserId, projectId, itemId);

        return new ResponseEntity<>(new InputStreamResource(inputStream), HttpStatus.OK);
    }

}
