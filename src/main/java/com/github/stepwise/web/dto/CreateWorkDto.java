package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.github.stepwise.entity.ProjectType;
import com.github.stepwise.utils.annotation.EnumValidator;
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
public class CreateWorkDto {

  @NotNull(message = "title is required")
  @NotBlank(message = "title must not be blank")
  @Size(min = 3, max = 50, message = "title must be between 3 and 50 characters")
  private String title;

  @Size(min = 3, max = 300,
      message = "description must be between 3 and 300 characters if provided")
  private String description;

  // @EnumValidator(enumClass = ProjectType.class, message = "Invalid project type")
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
