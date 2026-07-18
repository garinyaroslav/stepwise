package com.github.stepwise.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.web.dto.GroupResponseDto;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {

    @Query("SELECT new com.github.stepwise.web.dto.GroupResponseDto(g.id, g.name, SIZE(g.students)) FROM StudyGroup g")
    List<GroupResponseDto> findAllSummaries();

    @Query("""
            SELECT new com.github.stepwise.web.dto.GroupResponseDto(g.id, g.name, SIZE(g.students))
            FROM StudyGroup g
            WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    List<GroupResponseDto> findSummariesByNameContaining(@Param("search") String search);

    @Query("SELECT g FROM StudyGroup g LEFT JOIN FETCH g.students WHERE g.id = :id")
    Optional<StudyGroup> findByIdWithStudents(@Param("id") Long id);

}
