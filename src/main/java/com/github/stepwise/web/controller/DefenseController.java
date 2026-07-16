package com.github.stepwise.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.DefenseService;
import com.github.stepwise.web.dto.DefenseDto;
import com.github.stepwise.web.dto.DefenseDto.CreateScheduleDto;
import com.github.stepwise.web.dto.DefenseDto.RegistrationResponseDto;
import com.github.stepwise.web.dto.DefenseDto.ScheduleResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/defense")
@RequiredArgsConstructor
public class DefenseController {

    private final DefenseService defenseService;

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ScheduleResponseDto> createSchedule(@Valid @RequestBody CreateScheduleDto dto,
            @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defenseService.createSchedule(dto));
    }

    @DeleteMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ScheduleResponseDto> deleteSchedule(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(defenseService.deleteSchedule(scheduleId));
    }

    @GetMapping("/schedule/{scheduleId}/registrations")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_STUDENT')")
    public ResponseEntity<List<DefenseDto.RegistrationDetailsDto>> getRegistrations(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(defenseService.getRegistrationsForSchedule(scheduleId));
    }

    @GetMapping("/schedule/work/{academicWorkId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_STUDENT')")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByWork(@PathVariable Long academicWorkId) {
        return ResponseEntity.ok(defenseService.getSchedulesByWork(academicWorkId));
    }

    @PostMapping("/register/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RegistrationResponseDto> register(@PathVariable Long scheduleId,
            @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(defenseService.register(scheduleId, principal.getId()));
    }

    @GetMapping("/registration/work/{academicWorkId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RegistrationResponseDto> getMyRegistration(@PathVariable Long academicWorkId,
            @AuthenticationPrincipal AppUserDetails principal) {
        return ResponseEntity.ok(defenseService.getMyRegistration(academicWorkId, principal.getId()));
    }

}
