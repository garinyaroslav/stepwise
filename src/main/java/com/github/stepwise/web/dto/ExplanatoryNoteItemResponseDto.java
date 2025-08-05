package com.github.stepwise.web.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExplanatoryNoteItemResponseDto {

  private Long id;

  private Integer orderNumber;

  private ItemStatus status = ItemStatus.DRAFT;

  private String fileName;

  private String teacherComment;

  private LocalDateTime draftedAt;

  private LocalDateTime submittedAt;

  private LocalDateTime approvedAt;

  private LocalDateTime rejectedAt;

}
