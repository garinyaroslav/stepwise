package com.github.stepwise.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    public WorkTemplate(String workTitle, String workDescription, ProjectType type, Integer countOfChapters) {
        this.workTitle = workTitle;
        this.workDescription = workDescription;
        this.type = type;
        this.countOfChapters = countOfChapters;
    }

}
