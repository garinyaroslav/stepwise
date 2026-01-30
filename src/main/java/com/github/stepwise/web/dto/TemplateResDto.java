package com.github.stepwise.web.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.ProjectType;
import com.github.stepwise.entity.WorkTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateResDto {

    private Long id;

    private String title;

    private String description;

    private String workTitle;

    private String workDescription;

    private ProjectType type;

    private List<WorkChapterDto> chapters;

    public static TemplateResDto fromEntity(WorkTemplate work) {
        List<WorkChapterDto> chapters = work.getWorkTemplateChapters().stream()
                .map(WorkChapterDto::fromEntity)
                .toList();

        return TemplateResDto.builder()
                .id(work.getId())
                .title(work.getTemplateTitle())
                .description(work.getTemplateDescription())
                .workTitle(work.getWorkTitle())
                .workDescription(work.getWorkDescription())
                .type(work.getType())
                .chapters(chapters)
                .build();
    }
}
