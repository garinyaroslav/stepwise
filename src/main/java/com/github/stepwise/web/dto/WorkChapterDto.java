package com.github.stepwise.web.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkChapterDto {

    @NotNull(message = "index is required")
    @Positive(message = "index must be positive")
    private Integer index;

    @NotBlank(message = "title must not be blank")
    @Size(min = 3, max = 50, message = "title must be between 3 and 50 characters")
    private String title;

    @Size(min = 3, max = 300, message = "description must be between 3 and 300 characters if provided")
    private String description;

    @NotNull(message = "deadline is required")
    @Future(message = "deadline must be a future date")
    private LocalDateTime deadline;

}
