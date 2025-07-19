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

  @NotBlank(message = "Имя пользователя не может быть пустым")
  @Size(min = 3, max = 20, message = "Имя пользователя должно быть от 3 до 20 символов")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$",
      message = "Имя пользователя может содержать только буквы, цифры и подчеркивания")
  private String username;

  @NotBlank(message = "Пароль не может быть пустым")
  @Size(min = 6, max = 100, message = "Пароль должен быть от 6 до 100 символов")
  private String password;

}
