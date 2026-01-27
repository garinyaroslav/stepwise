package com.github.stepwise.web.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.service.WorkTemplateService;
import com.github.stepwise.web.dto.CreateWorkTemplateDto;
import com.github.stepwise.web.dto.PageResponse;
import com.github.stepwise.web.dto.TemplateResDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/teamplate")
@RequiredArgsConstructor
@Slf4j
public class WorkTemplateController {

    private final WorkTemplateService workTemplateService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER')")
    public ResponseEntity<Void> createWorkTemplate(@Valid @RequestBody CreateWorkTemplateDto reqDto) {
        workTemplateService.create(reqDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER')")
    public ResponseEntity<Void> deleteWorkTemplate(@Positive @PathVariable Long id) {
        workTemplateService.delete(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_TEACHER')")
    public ResponseEntity<PageResponse<TemplateResDto>> getAllTemplates(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        log.info("Fetching all templates, pageNumber: {}, pageSize: {}, search: {}", pageNumber, pageSize, search);
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        Page<WorkTemplate> templates = workTemplateService.findAllWithSearch(pageRequest, search);

        List<TemplateResDto> content = templates.stream()
                .map(TemplateResDto::fromEntity)
                .toList();

        return ResponseEntity.ok(new PageResponse<>(content, templates.getTotalPages()));
    }

}
