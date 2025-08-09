package com.github.stepwise.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RejectItemDto {

  @NotNull(message = "teacherComment is required")
  @NotBlank(message = "teacherComment must not be blank")
  @Size(max = 400, message = "teacherComment must not exceed 400 characters")
  private String teacherComment;

}
