package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.github.stepwise.entity.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkTemplateDto {

    @NotNull(message = "title is required")
    @NotBlank(message = "title must not be blank")
    @Size(min = 3, max = 100, message = "title must be between 3 and 100 characters")
    private String title;

    @Size(min = 3, max = 500, message = "description must be between 3 and 500 characters if provided")
    private String description;

    private ProjectType type;

    @NotNull(message = "groupId is required")
    @Positive(message = "groupId must be positive")
    private Long groupId;

    @NotNull(message = "teacherId is required")
    @Positive(message = "teahcerId must be positive")
    private Long teacherId;

    @NotEmpty(message = "chapters cannot be empty")
    private List<WorkChapterDto> chapters = new ArrayList<>();

}
