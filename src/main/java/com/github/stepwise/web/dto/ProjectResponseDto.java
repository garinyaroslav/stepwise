package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProjectResponseDto {

  private Long id;

  private String title;

  private String description;

  private UserResponseDto owner;

  private List<ExplanatoryNoteItemResponseDto> items = new ArrayList<>();

  private boolean isApprovedForDefense = false;

}
