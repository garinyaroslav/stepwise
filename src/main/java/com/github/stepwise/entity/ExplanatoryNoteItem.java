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
@Table(name = "explanatory_note_item")
public class ExplanatoryNoteItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer orderNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status = ItemStatus.DRAFT;

    @Builder.Default
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemHistory> history = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "student_project_id", nullable = false)
    private Project project;

    public ExplanatoryNoteItem(Integer orderNumber, ItemStatus status, Project project) {
        this.orderNumber = orderNumber;
        this.status = status;
        this.project = project;
        this.history = new ArrayList<>();
    }

    public String getLatestFileName() {
        return history.stream()
                .filter(h -> h.getFileName() != null)
                .reduce((first, second) -> second)
                .map(ItemHistory::getFileName)
                .orElse(null);
    }

}
