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
  private String title;

  @Column
  private int orderNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ItemStatus status = ItemStatus.DRAFT;

  @Column
  private String filePath;

  @Column
  private LocalDateTime submittedAt;

  @Column
  private LocalDateTime approvedAt;

  @ManyToOne
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;
}
