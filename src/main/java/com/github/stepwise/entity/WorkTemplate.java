package com.github.stepwise.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.annotation.CreatedDate;

import com.github.stepwise.audit.Auditable;

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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_template")
@Audited
public class WorkTemplate extends Auditable {

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

    // @CreatedDate
    // @Column(name = "created_at", nullable = false, updatable = false,
    // columnDefinition = "timestamp(6) default now()")
    // private LocalDateTime createdAtTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;

    @ManyToOne
    @JoinColumn(name = "teacher_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    private User teacher;

    @Builder.Default
    @OneToMany(mappedBy = "workTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotAudited
    private List<AcademicWork> academicWorks = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "workTemplate", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @NotAudited
    private List<WorkTemplateChapter> workTemplateChapters = new ArrayList<>();
}
