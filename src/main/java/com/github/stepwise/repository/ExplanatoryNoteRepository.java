package com.github.stepwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.ExplanatoryNoteItem;

public interface ExplanatoryNoteRepository extends JpaRepository<ExplanatoryNoteItem, Long> {
}
