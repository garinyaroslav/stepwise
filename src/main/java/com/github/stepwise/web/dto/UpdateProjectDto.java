package com.github.stepwise.web.dto;

import com.github.stepwise.entity.Project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProjectDto {

    @NotNull(message = "id is required")
    @Positive(message = "id must be a positive number")
    private Long id;

    @NotNull(message = "title is required")
    @NotBlank(message = "title must not be blank")
    private String title;

    @NotNull(message = "description is required")
    @NotBlank(message = "description must not be blank")
    private String description;

    public static UpdateProjectDto fromEntity(Project project) {
        return UpdateProjectDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .build();
    }

    public Project toEntity() {
        return Project.builder()
                .id(this.id)
                .title(this.title)
                .description(this.description)
                .build();
    }
}
