package com.github.stepwise.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.DefenseRegistration;
import com.github.stepwise.entity.DefenseSchedule;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.ProjectStatus;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.DefenceRegistrationRepository;
import com.github.stepwise.repository.DefenseScheduleRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.web.dto.DefenseDto.CreateScheduleDto;
import com.github.stepwise.web.dto.DefenseDto.RegistrationResponseDto;
import com.github.stepwise.web.dto.DefenseDto.ScheduleResponseDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefenseService {

    private final DefenseScheduleRepository scheduleRepository;

    private final DefenceRegistrationRepository registrationRepository;

    private final AcademicWorkRepository academicWorkRepository;

    private final ProjectRepository projectRepository;

    @Transactional
    public ScheduleResponseDto createSchedule(CreateScheduleDto dto) {
        log.info("Creating defense schedule for academicWorkId: {}, startTime: {}", dto.getAcademicWorkId(),
                dto.getStartTime());

        AcademicWork work = academicWorkRepository.findById(dto.getAcademicWorkId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Academic work not found: %s".formatted(dto.getAcademicWorkId())));

        DefenseSchedule schedule = DefenseSchedule.builder()
                .academicWork(work)
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .maxStudents(dto.getMaxStudents())
                .comment(dto.getComment())
                .build();

        scheduleRepository.save(schedule);
        log.info("Defense schedule created with id: {}", schedule.getId());

        return ScheduleResponseDto.fromEntity(schedule, 0);
    }

    public List<ScheduleResponseDto> getSchedulesByWork(Long academicWorkId) {
        log.info("Fetching defense schedules for academicWorkId: {}", academicWorkId);

        return scheduleRepository
                .findByAcademicWorkIdAndIsActiveTrue(academicWorkId)
                .stream()
                .map(s -> {
                    int count = scheduleRepository.countRegistrations(s.getId());
                    return ScheduleResponseDto.fromEntity(s, count);
                })
                .toList();
    }

    @Transactional
    public RegistrationResponseDto register(Long scheduleId, Long studentId) {
        log.info("Student {} registering for scheduleId: {}", studentId, scheduleId);

        DefenseSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: %s".formatted(scheduleId)));

        Long academicWorkId = schedule.getAcademicWork().getId();

        boolean alreadyRegistered = registrationRepository.existsByStudentIdAndAcademicWorkId(studentId,
                academicWorkId);
        if (alreadyRegistered)
            throw new IllegalStateException(
                    "Student is already registered for a defense session of this work");

        Project project = projectRepository
                .findByStudentIdAndAcademicWorkId(studentId, academicWorkId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Project not found for student %s and work %s".formatted(studentId, academicWorkId)));

        if (project.getStatus() != ProjectStatus.APPROVED_FOR_DEFENSE)
            throw new IllegalStateException("Student is not approved for defense yet");

        int currentCount = scheduleRepository.countRegistrations(scheduleId);

        if (schedule.getMaxStudents() != null && currentCount >= schedule.getMaxStudents())
            throw new IllegalStateException("This defense session is full");

        DefenseRegistration registration = DefenseRegistration.builder()
                .defenseSchedule(schedule)
                .project(project)
                .registeredAt(LocalDateTime.now())
                .orderNumber(currentCount + 1)
                .build();

        registrationRepository.save(registration);
        log.info("Student {} registered for scheduleId: {}, orderNumber: {}",
                studentId, scheduleId, registration.getOrderNumber());

        return RegistrationResponseDto.fromEntity(registration);
    }

    public RegistrationResponseDto getMyRegistration(Long academicWorkId, Long studentId) {
        log.info("Fetching registration for student: {}, academicWorkId: {}", studentId, academicWorkId);

        return registrationRepository
                .findByStudentIdAndAcademicWorkId(studentId, academicWorkId)
                .map(RegistrationResponseDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No registration found for student %s and work %s".formatted(studentId, academicWorkId)));
    }
}
