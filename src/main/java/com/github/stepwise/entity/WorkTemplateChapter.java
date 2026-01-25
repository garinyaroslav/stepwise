package com.github.stepwise.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_template_chapter")
public class WorkTemplateChapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer indexOfChapter;

    @Column
    private String description;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @ManyToOne
    @JoinColumn(name = "academic_work_id", nullable = false)
    private WorkTemplate workTemplate;

    public WorkTemplateChapter(String title, Integer indexOfChapter, String description,
            WorkTemplate workTemplate, LocalDateTime deadline) {
        this.title = title;
        this.indexOfChapter = indexOfChapter;
        this.description = description;
        this.workTemplate = workTemplate;
        this.deadline = deadline;
    }

}
