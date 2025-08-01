package com.github.stepwise.web.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.stepwise.entity.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkResponseDto {

  private Long id;

  private String title;

  private String description;

  private Integer countOfChapters;

  private ProjectType type;

  private String teacherEmail;

  private String teacherName;

  private String teacherLastName;

  private String teacherMiddleName;

  private List<WorkChapterDto> academicWorkChapters;

}
