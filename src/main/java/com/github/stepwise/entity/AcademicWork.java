package com.github.stepwise.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "academic_work")
public class AcademicWork {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(nullable = false)
  private String countOfChapters;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProjectType type;

  @ManyToOne
  @JoinColumn(name = "teacher_id")
  private User teacher;

  @ManyToOne
  @JoinColumn(name = "group_id", nullable = false)
  private StudyGroup group;

  @OneToMany(mappedBy = "academicWork", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AcademicWorkChapter> academicWorkChapters = new ArrayList<>();

  @OneToMany(mappedBy = "academicWork", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Project> projects = new ArrayList<>();

}
