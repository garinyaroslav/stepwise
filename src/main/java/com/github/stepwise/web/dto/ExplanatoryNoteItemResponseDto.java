package com.github.stepwise.web.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.ExplanatoryNoteItem;
import com.github.stepwise.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExplanatoryNoteItemResponseDto {

    private Long id;

    private Integer orderNumber;

    @Builder.Default
    private ItemStatus status = ItemStatus.DRAFT;

    private String fileName;

    private String teacherComment;

    private LocalDateTime draftedAt;

    private LocalDateTime submittedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

    public static ExplanatoryNoteItemResponseDto fromEntity(ExplanatoryNoteItem item) {
        return ExplanatoryNoteItemResponseDto.builder()
                .id(item.getId())
                .orderNumber(item.getOrderNumber())
                .status(item.getStatus())
                .fileName(item.getFileName())
                .teacherComment(item.getTeacherComment())
                .draftedAt(item.getDraftedAt())
                .submittedAt(item.getSubmittedAt())
                .approvedAt(item.getApprovedAt())
                .rejectedAt(item.getRejectedAt())
                .build();
    }
}
