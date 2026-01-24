package com.github.stepwise.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@NoArgsConstructor
@Builder
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

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "academic_work_id", nullable = false)
    private AcademicWork academicWork;

    @Builder.Default
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ExplanatoryNoteItem> items = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean isApprovedForDefense = false;

    @Column
    private LocalDateTime approvedForDefenseAt;

    public Project(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public Project(String title, String description, User student, AcademicWork academicWork) {
        this.title = title;
        this.description = description;
        this.student = student;
        this.academicWork = academicWork;
    }
}
