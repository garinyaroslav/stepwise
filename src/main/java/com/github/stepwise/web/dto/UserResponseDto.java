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

  private String middleName;

  private String phoneNumber;

  private String address;

  public UserResponseDto(Long id) {
    this.id = id;
  }

  public UserResponseDto(String username, String email) {
    this.username = username;
    this.email = email;
  }

  public UserResponseDto(Long id, String role) {
    this.id = id;
    this.role = role;
  }

  public UserResponseDto(Long id, String username, String email, String firstName, String lastName,
      String middleName) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.middleName = middleName;
  }

  public UserResponseDto(Long id, String username, String email, String firstName, String lastName,
      String middleName, String phoneNumber, String address) {
    this.id = id;
    this.username = username;
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.middleName = middleName;
    this.phoneNumber = phoneNumber;
    this.address = address;
  }

}
