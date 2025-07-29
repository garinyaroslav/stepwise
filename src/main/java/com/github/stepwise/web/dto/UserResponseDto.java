package com.github.stepwise.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {

  private Long id;

  private String username;

  private String email;

  private String role;

  private String firstName;

  private String lastName;

  private String phoneNumber;

  private String address;

}
