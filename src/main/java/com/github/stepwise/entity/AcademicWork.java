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
    private Integer countOfChapters;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private StudyGroup group;

    @Builder.Default
    @OneToMany(mappedBy = "academicWork", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AcademicWorkChapter> academicWorkChapters = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "academicWork", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects = new ArrayList<>();

    public AcademicWork(String title, String description, ProjectType type, Integer countOfChapters) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.countOfChapters = countOfChapters;
    }

}
