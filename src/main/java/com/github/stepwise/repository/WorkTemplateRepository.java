package com.github.stepwise.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.stepwise.entity.WorkTemplate;

public interface WorkTemplateRepository extends JpaRepository<WorkTemplate, Long> {

    @Query("SELECT wt FROM WorkTemplate wt WHERE " +
            "LOWER(wt.workTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(wt.workDescription) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(wt.templateTitle) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(wt.templateDescription) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<WorkTemplate> findAllWithSearch(Pageable pageable, @Param("search") String search);

}
