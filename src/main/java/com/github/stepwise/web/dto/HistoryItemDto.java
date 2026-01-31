package com.github.stepwise.web.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.ItemHistory;
import com.github.stepwise.entity.ItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class HistoryItemDto {

    private Long id;

    private ItemStatus previousStatus;

    private ItemStatus newStatus;

    private String teacherComment;

    private LocalDateTime changedAt;

    private UserResponseDto changedBy;

    public static HistoryItemDto fromEntity(ItemHistory item) {
        return HistoryItemDto.builder()
                .id(item.getId())
                .previousStatus(item.getPreviousStatus())
                .newStatus(item.getNewStatus())
                .teacherComment(item.getTeacherComment())
                .changedAt(item.getChangedAt())
                .changedBy(UserResponseDto.fromUser(item.getChangedBy()))
                .build();
    }
}
