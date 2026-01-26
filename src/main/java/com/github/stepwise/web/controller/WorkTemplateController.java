package com.github.stepwise.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.service.WorkTemplateService;
import com.github.stepwise.web.dto.CreateWorkTemplateDto;

import jakarta.validation.Valid;
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
    public ResponseEntity<Void> getAllStudents(@Valid @RequestBody CreateWorkTemplateDto reqDto) {
        workTemplateService.create(reqDto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
