package com.github.stepwise.web.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkResponseDto {

    private Long id;

    private String title;

    private String description;

    private Integer countOfChapters;

    private ProjectType type;

    private String teacherEmail;

    private String teacherName;

    private String teacherLastName;

    private String teacherMiddleName;

    private String groupName;

    private List<WorkChapterDto> chapters;

    public static WorkResponseDto fromEntity(AcademicWork work) {
        return WorkResponseDto.builder()
                .id(work.getId())
                .title(work.getTitle())
                .description(work.getDescription())
                .countOfChapters(work.getCountOfChapters())
                .type(work.getType())
                .teacherEmail(work.getTeacher().getEmail())
                .teacherName(work.getTeacher().getProfile().getFirstName())
                .teacherLastName(work.getTeacher().getProfile().getLastName())
                .teacherMiddleName(work.getTeacher().getProfile().getMiddleName())
                .groupName(work.getGroup().getName())
                .build();
    }

    public static WorkResponseDto fromEntityWithChapters(AcademicWork work) {
        List<WorkChapterDto> chapters = work.getAcademicWorkChapters().stream()
                .map(WorkChapterDto::fromEntity)
                .toList();

        return WorkResponseDto.builder()
                .id(work.getId())
                .title(work.getTitle())
                .description(work.getDescription())
                .countOfChapters(work.getCountOfChapters())
                .type(work.getType())
                .teacherEmail(work.getTeacher().getEmail())
                .teacherName(work.getTeacher().getProfile().getFirstName())
                .teacherLastName(work.getTeacher().getProfile().getLastName())
                .teacherMiddleName(work.getTeacher().getProfile().getMiddleName())
                .groupName(work.getGroup().getName())
                .chapters(chapters)
                .build();
    }
}
