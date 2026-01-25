package com.github.stepwise.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkDto {

    @NotNull(message = "groupId is required")
    @Positive(message = "groupId must be positive")
    private Long groupId;

    @NotNull(message = "workTemplateId is required")
    @Positive(message = "workTemplateId must be positive")
    private Long workTemplateId;

}
