package com.github.stepwise.web.dto;

import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponseDto {

    private Long id;

    private String title;

    private String description;

    private UserResponseDto owner;

    @Builder.Default
    private List<ExplanatoryNoteItemResponseDto> items = new ArrayList<>();

    private boolean isApprovedForDefense;

    public static ProjectResponseDto fromEntity(Project project) {
        return fromEntity(project, false);
    }

    public static ProjectResponseDto fromEntity(Project project, boolean excludeDrafts) {
        List<ExplanatoryNoteItemResponseDto> items = project.getItems().stream()
                .filter(item -> !excludeDrafts || item.getStatus() != ItemStatus.DRAFT)
                .map(ExplanatoryNoteItemResponseDto::fromEntity)
                .collect(Collectors.toList());

        UserResponseDto owner = UserResponseDto.fromUser(project.getStudent());

        return ProjectResponseDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .owner(owner)
                .items(items)
                .isApprovedForDefense(project.isApprovedForDefense())
                .build();
    }

    public static ProjectResponseDto fromEntityBasic(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .isApprovedForDefense(project.isApprovedForDefense())
                .build();
    }
}
