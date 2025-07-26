package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {

  @Pattern(regexp = "[А-Яа-яA-Za-z]{4}-\\d{3}",
      message = "Group naNameme must be in format: 4 letters, hyphen, 3 digits (e.g., ПИН-122 or ПМИз-123)")
  private String name;

  @NotEmpty(message = "Students ids cannot be empty")
  private List<Long> studentIds = new ArrayList<>();

}
