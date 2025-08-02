package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudyGroupResponseDto {

  private String name;

  private List<UserResponseDto> students = new ArrayList<>();

}
