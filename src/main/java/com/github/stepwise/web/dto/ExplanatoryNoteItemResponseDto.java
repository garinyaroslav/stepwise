package com.github.stepwise.web.dto;

import java.util.List;

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

    private List<HistoryItemDto> history;

    public static ExplanatoryNoteItemResponseDto fromEntity(ExplanatoryNoteItem item) {
        return ExplanatoryNoteItemResponseDto.builder()
                .id(item.getId())
                .orderNumber(item.getOrderNumber())
                .status(item.getStatus())
                .fileName(item.getFileName())
                .history(item.getHistory().stream()
                        .map(HistoryItemDto::fromEntity)
                        .toList())
                .build();
    }
}
