package com.github.stepwise.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

  @NotBlank(message = "useranme is required")
  @Size(min = 3, max = 20, message = "useranme must be between 3 and 20 characters")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$",
      message = "useranme can only contain letters, numbers, and underscores")
  private String username;

  @NotBlank(message = "password is required")
  @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
  private String password;

}
