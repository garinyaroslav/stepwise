package com.github.stepwise.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "item_history")
public class ItemHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private ExplanatoryNoteItem item;

    @Enumerated(EnumType.STRING)
    @Column
    private ItemStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus newStatus;

    @Column(columnDefinition = "TEXT")
    private String teacherComment;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy;

}
