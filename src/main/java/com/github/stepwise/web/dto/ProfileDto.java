package com.github.stepwise.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {

  @Positive(message = "id must be a positive number")
  private Long id;

  @Size(min = 2, max = 20, message = "firstName must be between 2 and 20 characters")
  private String firstName;

  @Size(min = 2, max = 20, message = "lastName must be between 2 and 20 characters")
  private String lastName;

  @Size(min = 2, max = 20, message = "middleName must be between 2 and 20 characters")
  private String middleName;

  @NotBlank(message = "phoneNumber must not be blank")
  @Pattern(regexp = "^(\\+7|8)[0-9]{10}$",
      message = "phoneNumber must be a valid phone number starting with +7 or 8 followed by 10 digits")
  private String phoneNumber;


  @Size(min = 2, max = 20, message = "address must be between 2 and 40 characters")
  private String address;

}
