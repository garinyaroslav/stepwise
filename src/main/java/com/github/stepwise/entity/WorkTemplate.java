package com.github.stepwise.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Table(name = "work_template")
@EntityListeners(AuditingEntityListener.class)
public class WorkTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String templateTitle;

    @Column(columnDefinition = "TEXT")
    private String templateDescription;

    @Column(nullable = false)
    private String workTitle;

    @Column(columnDefinition = "TEXT")
    private String workDescription;

    @Column(nullable = false)
    private Integer countOfChapters;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Builder.Default
    @OneToMany(mappedBy = "workTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcademicWork> academicWorks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<WorkTemplateChapter> workTemplateChapters = new ArrayList<>();

}
