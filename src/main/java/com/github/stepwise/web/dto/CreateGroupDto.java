package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.github.stepwise.utils.annotation.NoDublicatesInCollection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupDto {

  @NotNull(message = "name of group is required")
  @NotBlank(message = "name of group must not be blank")
  @Pattern(regexp = "[А-Яа-яA-Za-z]{1,4}-\\d{3}",
      message = "name of group must be in format: 4 letters, hyphen, 3 digits (e.g., ПИН-122 or ПМИз-123)")
  private String name;

  @NotNull(message = "Students ids is required")
  @NotEmpty(message = "Students ids cannot be empty")
  @NoDublicatesInCollection(message = "Student IDs must be unique")
  private List<@NotNull(message = "Student ID cannot be null") @Positive(
      message = "Student ID must be a positive number") Long> studentIds = new ArrayList<>();

}
