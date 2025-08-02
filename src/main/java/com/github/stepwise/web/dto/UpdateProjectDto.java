package com.github.stepwise.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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

}
