package com.github.stepwise.web.controller;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.github.stepwise.security.AppUserDetails;
import com.github.stepwise.service.DefenseService;
import com.github.stepwise.web.dto.DefenseDto.CreateScheduleDto;
import com.github.stepwise.web.dto.DefenseDto.RegistrationResponseDto;
import com.github.stepwise.web.dto.DefenseDto.ScheduleResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/defense")
@RequiredArgsConstructor
@Slf4j
public class DefenseController {

    private final DefenseService defenseService;

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN')")
    public ResponseEntity<ScheduleResponseDto> createSchedule(@Valid @RequestBody CreateScheduleDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Teacher {} creating defense schedule for workId: {}", ((AppUserDetails) userDetails).getId(),
                dto.getAcademicWorkId());

        return ResponseEntity.status(HttpStatus.CREATED).body(defenseService.createSchedule(dto));
    }

    @GetMapping("/schedule/work/{academicWorkId}")
    @PreAuthorize("hasAnyRole('ROLE_TEACHER', 'ROLE_ADMIN', 'ROLE_STUDENT')")
    public ResponseEntity<List<ScheduleResponseDto>> getSchedulesByWork(@PathVariable Long academicWorkId) {
        log.info("Fetching defense schedules for workId: {}", academicWorkId);

        return ResponseEntity.ok(defenseService.getSchedulesByWork(academicWorkId));
    }

    @PostMapping("/register/{scheduleId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RegistrationResponseDto> register(@PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUserDetails appUser = (AppUserDetails) userDetails;
        log.info("Student: {} registering for scheduleId: {}", appUser.getId(), scheduleId);

        return ResponseEntity.status(HttpStatus.CREATED).body(defenseService.register(scheduleId, appUser.getId()));
    }

    @GetMapping("/registration/work/{academicWorkId}")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    public ResponseEntity<RegistrationResponseDto> getMyRegistration(@PathVariable Long academicWorkId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUserDetails appUser = (AppUserDetails) userDetails;
        log.info("Student {} fetching own registration for workId: {}", appUser.getId(), academicWorkId);

        return ResponseEntity.ok(defenseService.getMyRegistration(academicWorkId, appUser.getId()));
    }
}
