package com.github.stepwise.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "explanatory_note_item")
public class ExplanatoryNoteItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer orderNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ItemStatus status = ItemStatus.DRAFT;

  @Column(nullable = false)
  private String fileName;

  @Column(columnDefinition = "TEXT")
  private String teacherComment;

  @Column(nullable = false)
  private LocalDateTime draftedAt;

  @Column
  private LocalDateTime submittedAt;

  @Column
  private LocalDateTime approvedAt;

  @Column
  private LocalDateTime rejectedAt;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  public ExplanatoryNoteItem(Integer orderNumber, ItemStatus status, String fileName,
      LocalDateTime draftedAt, Project project) {
    this.orderNumber = orderNumber;
    this.status = status;
    this.fileName = fileName;
    this.draftedAt = draftedAt;
    this.project = project;
  }

}
