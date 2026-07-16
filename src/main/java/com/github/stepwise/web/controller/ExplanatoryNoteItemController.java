package com.github.stepwise.web.controller;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.ExplanatoryNoteItemService;
import com.github.stepwise.service.GigaChatService;
import com.github.stepwise.web.dto.TeacherCommentDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/explanatory-note-item")
@RequiredArgsConstructor
public class ExplanatoryNoteItemController {

    private final ExplanatoryNoteItemService explanatoryNoteItemService;

    private final GigaChatService gigaChatService;

    @PostMapping(path = "/draft", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<Void> createExplanatoryNoteItem(@RequestPart("projectId") String projectId,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal AppUserDetails principal) throws Exception {
        explanatoryNoteItemService.draftItem(principal.getId(), Long.valueOf(projectId), file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{id}/submition")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<Void> submitExplanatoryNoteItem(@PathVariable Long id,
            @AuthenticationPrincipal AppUserDetails principal) {
        explanatoryNoteItemService.submitItem(id, principal.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/approval")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<Void> approveExplanatoryNoteItem(@PathVariable Long id,
            @RequestBody TeacherCommentDto teacherCommentDto,
            @AuthenticationPrincipal AppUserDetails principal) {
        explanatoryNoteItemService.approveItem(id, principal.getId(), teacherCommentDto.getTeacherComment());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/rejection")
    @PreAuthorize("hasRole('ROLE_TEACHER')")
    public ResponseEntity<Void> rejectExplanatoryNoteItem(@PathVariable Long id,
            @Valid @RequestBody TeacherCommentDto teacherCommentDto,
            @AuthenticationPrincipal AppUserDetails principal) {
        explanatoryNoteItemService.rejectItem(id, principal.getId(), teacherCommentDto.getTeacherComment());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<InputStreamResource> downloadItemFile(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam(required = false) Long userId,
            @RequestParam Long projectId,
            @RequestParam Long itemId,
            @RequestParam(required = false) Long historyId) throws Exception {
        Long targetUserId = explanatoryNoteItemService.resolveAccessibleUserId(userId, principal.getId(),
                principal.getRole());

        InputStream inputStream = explanatoryNoteItemService.getItemFile(targetUserId, projectId, itemId, historyId);
        String fileName = explanatoryNoteItemService.getItemFileName(itemId, historyId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''"
                                + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(new InputStreamResource(inputStream));
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> getItemSummary(
            @AuthenticationPrincipal AppUserDetails principal,
            @RequestParam Long studentId,
            @RequestParam Long projectId,
            @RequestParam Long itemId,
            @RequestParam Long historyId) {
        explanatoryNoteItemService.resolveAccessibleUserId(studentId, principal.getId(), principal.getRole());

        String fileName = explanatoryNoteItemService.getItemFileName(itemId, historyId);
        String summary = gigaChatService.summarizeReport(studentId, projectId, itemId, historyId, fileName);

        return ResponseEntity.ok(Map.of("summary", summary));
    }

}
