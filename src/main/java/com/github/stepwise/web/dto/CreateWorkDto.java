package com.github.stepwise.web.dto;

import java.util.ArrayList;
import java.util.List;
import com.github.stepwise.entity.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWorkDto {

  private String title;

  private String description;

  private ProjectType type;

  private Long groupId;

  private Long teacherId;

  private List<WorkChapterDto> chapters = new ArrayList<>();

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  static class WorkChapterDto {

    private Integer index;

    private String title;

    private String description;

  }


}
