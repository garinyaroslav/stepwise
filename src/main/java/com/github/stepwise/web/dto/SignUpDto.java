package com.github.stepwise.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.github.stepwise.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpDto {

  @NotNull(message = "useranme is required")
  @NotBlank(message = "useranme must not be blank")
  @Size(min = 3, max = 20, message = "useranme must be between 3 and 20 characters")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$",
      message = "useranme can only contain letters, numbers, and underscores")
  private String username;

  @NotNull(message = "email is required")
  @NotBlank(message = "email must not be blank")
  @Pattern(
      regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$",
      message = "email is not valid")
  private String email;

  @NotNull(message = "password is required")
  @NotBlank(message = "password must not be blank")
  @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
  @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$",
      message = "Password is not valid: password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
  private String password;

  private UserRole role;
}
