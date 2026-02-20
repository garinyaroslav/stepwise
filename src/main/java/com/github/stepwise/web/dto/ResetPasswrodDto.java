package com.github.stepwise.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswrodDto {

    @NotNull(message = "token is required")
    @NotBlank(message = "token must not be blank")
    private String token;

    @NotNull(message = "newPassword is required")
    @NotBlank(message = "newPassword must not be blank")
    @Size(min = 8, max = 100, message = "newPassword must be between 6 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$", message = "newPassword is not valid: password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String newPassword;

}
