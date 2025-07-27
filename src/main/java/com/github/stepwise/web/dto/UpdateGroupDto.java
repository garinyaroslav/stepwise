package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.github.stepwise.utils.annotation.NoDublicatesInCollection;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroupDto {

  @NotNull(message = "Group ID is required")
  @Positive(message = "Group ID must be a positive number")
  private Long id;

  @NotEmpty(message = "Student IDs cannot be empty")
  @Size(min = 1, max = 100, message = "Student IDs list size must be between 1 and 100")
  @NoDublicatesInCollection(message = "Student IDs must be unique")
  private List<@NotNull(message = "Student ID cannot be null") @Positive(
      message = "Student ID must be a positive number") Long> studentIds = new ArrayList<>();

}
