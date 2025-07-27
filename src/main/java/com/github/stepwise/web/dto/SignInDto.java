
package com.github.stepwise.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignInDto {
  @NotBlank(message = "useranme is required")
  private String username;

  @NotBlank(message = "password is required")
  private String password;
}
