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
@Table(name = "project")
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  // @Enumerated(EnumType.STRING)
  // @Column(nullable = false)
  // private ProjectType type;

  // @ManyToOne
  // @JoinColumn(name = "group_id", nullable = false)
  // private StudyGroup group;

  @ManyToOne
  @JoinColumn(name = "student_id")
  private User student;

  @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ExplanatoryNoteItem> items = new ArrayList<>();

  @Column(nullable = false)
  private boolean isApprovedForDefense = false;
}
