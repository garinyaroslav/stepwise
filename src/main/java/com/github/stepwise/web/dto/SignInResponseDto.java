package com.github.stepwise.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInResponseDto {

    private UserResponseDto user;

    private String token;

    private boolean temporaryPassword;

}
