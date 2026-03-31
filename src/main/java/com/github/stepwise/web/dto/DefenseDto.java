package com.github.stepwise.web.dto;

import java.time.LocalDateTime;

import com.github.stepwise.entity.DefenseRegistration;
import com.github.stepwise.entity.DefenseSchedule;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class DefenseDto {

    @Data
    public static class CreateScheduleDto {

        @NotNull
        private Long academicWorkId;

        @NotNull
        private LocalDateTime startTime;

        private LocalDateTime endTime;

        private Integer maxStudents;

        private String comment;

    }

    @Data
    public static class ScheduleResponseDto {

        private Long id;

        private Long academicWorkId;

        private LocalDateTime startTime;

        private LocalDateTime endTime;

        private Integer maxStudents;

        private String comment;

        private boolean active;

        private int registeredCount;

        private boolean full;

        public static ScheduleResponseDto fromEntity(DefenseSchedule schedule, int registeredCount) {
            ScheduleResponseDto dto = new ScheduleResponseDto();
            dto.id = schedule.getId();
            dto.academicWorkId = schedule.getAcademicWork().getId();
            dto.startTime = schedule.getStartTime();
            dto.endTime = schedule.getEndTime();
            dto.maxStudents = schedule.getMaxStudents();
            dto.comment = schedule.getComment();
            dto.registeredCount = registeredCount;
            dto.full = schedule.getMaxStudents() != null && registeredCount >= schedule.getMaxStudents();
            return dto;
        }

    }

    @Data
    public static class RegistrationResponseDto {

        private Long id;

        private Long scheduleId;

        private LocalDateTime scheduleStartTime;

        private Long projectId;

        private LocalDateTime registeredAt;

        private Integer orderNumber;

        public static RegistrationResponseDto fromEntity(DefenseRegistration registration) {
            RegistrationResponseDto dto = new RegistrationResponseDto();
            dto.id = registration.getId();
            dto.scheduleId = registration.getDefenseSchedule().getId();
            dto.scheduleStartTime = registration.getDefenseSchedule().getStartTime();
            dto.projectId = registration.getProject().getId();
            dto.registeredAt = registration.getRegisteredAt();
            dto.orderNumber = registration.getOrderNumber();
            return dto;
        }

    }

    @Data
    public class DefendProjectDto {

        @NotNull
        @Min(1)
        @Max(5)
        private Integer grade;
    }

}
