package com.github.stepwise.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.DefenseRegistration;
import com.github.stepwise.entity.DefenseSchedule;
import com.github.stepwise.entity.Profile;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.ProjectStatus;
import com.github.stepwise.entity.User;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.DefenceRegistrationRepository;
import com.github.stepwise.repository.DefenseScheduleRepository;
import com.github.stepwise.repository.DefenseScheduleRepository.ScheduleRegistrationCount;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.web.dto.DefenseDto;
import com.github.stepwise.web.dto.DefenseDto.CreateScheduleDto;
import com.github.stepwise.web.dto.DefenseDto.RegistrationResponseDto;
import com.github.stepwise.web.dto.DefenseDto.ScheduleResponseDto;

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
        log.info("Creating defense schedule for academicWorkId: {}, startTime: {}",
                dto.getAcademicWorkId(), dto.getStartTime());

        AcademicWork work = academicWorkRepository.findById(dto.getAcademicWorkId())
                .orElseThrow(() -> new NotFoundException("Academic work not found: " + dto.getAcademicWorkId()));

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

    @Transactional
    public ScheduleResponseDto deleteSchedule(Long scheduleId) {
        DefenseSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + scheduleId));

        int regCount = scheduleRepository.countRegistrations(scheduleId);
        ScheduleResponseDto response = ScheduleResponseDto.fromEntity(schedule, regCount);

        scheduleRepository.delete(schedule);
        log.info("Deleted defense schedule id: {}, had {} registrations", scheduleId, regCount);
        return response;
    }

    public List<ScheduleResponseDto> getSchedulesByWork(Long academicWorkId) {
        log.info("Fetching defense schedules for academicWorkId: {}", academicWorkId);

        List<DefenseSchedule> schedules = scheduleRepository.findByAcademicWorkId(academicWorkId);
        if (schedules.isEmpty()) {
            return List.of();
        }

        Map<Long, Long> countsByScheduleId = scheduleRepository
                .countRegistrationsByScheduleIds(schedules.stream().map(DefenseSchedule::getId).toList())
                .stream()
                .collect(java.util.stream.Collectors.toMap(
                        ScheduleRegistrationCount::getScheduleId, ScheduleRegistrationCount::getCount));

        return schedules.stream()
                .map(s -> ScheduleResponseDto.fromEntity(s, countsByScheduleId.getOrDefault(s.getId(), 0L).intValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DefenseDto.RegistrationDetailsDto> getRegistrationsForSchedule(Long scheduleId) {
        log.info("Getting registrations by scheduleId: {}", scheduleId);

        if (!scheduleRepository.existsById(scheduleId)) {
            throw new NotFoundException("Schedule not found: " + scheduleId);
        }

        return registrationRepository.findByScheduleIdWithStudentDetails(scheduleId).stream()
                .sorted(Comparator.comparing(DefenseRegistration::getOrderNumber,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toRegistrationDetailsDto)
                .toList();
    }

    private DefenseDto.RegistrationDetailsDto toRegistrationDetailsDto(DefenseRegistration reg) {
        User student = reg.getProject().getStudent();
        Profile profile = student.getProfile();

        DefenseDto.RegistrationDetailsDto dto = new DefenseDto.RegistrationDetailsDto();
        dto.setRegistrationId(reg.getId());
        dto.setStudentId(student.getId());
        dto.setOrderNumber(reg.getOrderNumber());
        dto.setRegisteredAt(reg.getRegisteredAt());
        dto.setFirstName(profile != null ? profile.getFirstName() : null);
        dto.setLastName(profile != null ? profile.getLastName() : null);
        dto.setUsername(student.getUsername());
        return dto;
    }

    @Transactional
    public RegistrationResponseDto register(Long scheduleId, Long studentId) {
        log.info("Student {} registering for scheduleId: {}", studentId, scheduleId);

        DefenseSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found: " + scheduleId));

        Long academicWorkId = schedule.getAcademicWork().getId();

        Project project = projectRepository
                .findByStudentIdAndAcademicWorkId(studentId, academicWorkId)
                .orElseThrow(() -> new NotFoundException(
                        "Project not found for student %s and work %s".formatted(studentId, academicWorkId)));

        if (project.getStatus() == ProjectStatus.DEFENDED) {
            throw new IllegalStateException("Project is already defended");
        }
        if (project.getStatus() != ProjectStatus.APPROVED_FOR_DEFENSE) {
            throw new IllegalStateException("Student is not approved for defense yet");
        }

        registrationRepository
                .findByStudentIdAndAcademicWorkId(studentId, academicWorkId)
                .ifPresent(existing -> replacePreviousRegistration(existing, studentId));

        int currentCount = scheduleRepository.countRegistrations(scheduleId);
        if (schedule.getMaxStudents() != null && currentCount >= schedule.getMaxStudents()) {
            throw new IllegalStateException("This defense session is full");
        }

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

    private void replacePreviousRegistration(DefenseRegistration existing, Long studentId) {
        DefenseSchedule previousSchedule = existing.getDefenseSchedule();
        LocalDateTime previousEnd = previousSchedule.getEndTime() != null
                ? previousSchedule.getEndTime()
                : previousSchedule.getStartTime();

        if (LocalDateTime.now().isBefore(previousEnd)) {
            throw new IllegalStateException(
                    "Cannot re-register before the previous defense session ends at " + previousEnd);
        }

        log.info("Student {} previous defense session ended, removing old registration id: {}",
                studentId, existing.getId());
        registrationRepository.delete(existing);
        registrationRepository.flush();
    }

    public RegistrationResponseDto getMyRegistration(Long academicWorkId, Long studentId) {
        log.info("Fetching registration for student: {}, academicWorkId: {}", studentId, academicWorkId);

        return registrationRepository
                .findByStudentIdAndAcademicWorkId(studentId, academicWorkId)
                .map(RegistrationResponseDto::fromEntity)
                .orElseThrow(() -> new NotFoundException(
                        "No registration found for student %s and work %s".formatted(studentId, academicWorkId)));
    }

}
