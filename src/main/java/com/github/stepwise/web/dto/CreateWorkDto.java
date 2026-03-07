package com.github.stepwise.web.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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

    @NotNull(message = "deadlines are required")
    @Valid
    private List<ChapterDeadlineDto> deadlines;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterDeadlineDto {
        @NotNull
        @Min(0)
        private Integer chapterIndex;

        @NotNull
        private LocalDateTime deadline;
    }

}
