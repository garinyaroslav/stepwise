package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.github.stepwise.entity.ProjectType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWorkTemplateDto {

    @Size(min = 3, max = 100, message = "workTitle must be between 3 and 100 characters")
    private String workTitle;

    @Size(min = 3, max = 100, message = "templateTitle must be between 3 and 100 characters")
    private String templateTitle;

    @Size(min = 3, max = 500, message = "workDescription must be between 3 and 500 characters if provided")
    private String workDescription;

    @Size(min = 3, max = 500, message = "templateDescription must be between 3 and 500 characters if provided")
    private String templateDescription;

    private ProjectType type;

    @NotEmpty(message = "chapters cannot be empty")
    private List<WorkChapterDto> chapters = new ArrayList<>();

}
