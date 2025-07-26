package com.github.stepwise.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "study_group")
public class StudyGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  // @ManyToOne
  // @JoinColumn(name = "teacher_id")
  // private User teacher;

  @ManyToMany
  @JoinTable(name = "group_student", joinColumns = @JoinColumn(name = "group_id"),
      inverseJoinColumns = @JoinColumn(name = "student_id"))
  private List<User> students = new ArrayList<>();

  // @OneToMany(mappedBy = "group")
  // private List<Project> projects = new ArrayList<>();

  @OneToMany(mappedBy = "group")
  private List<AcademicWork> academicWorks = new ArrayList<>();
}

