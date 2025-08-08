package com.github.stepwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.stepwise.entity.ExplanatoryNoteItem;

public interface ExplanatoryNoteRepository extends JpaRepository<ExplanatoryNoteItem, Long> {
  @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " + "FROM ExplanatoryNoteItem e "
      + "WHERE e.id = :itemId AND e.project.student.id = :userId")
  boolean existsByIdAndUserId(@Param("itemId") Long itemId, @Param("userId") Long userId);

  @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END " + "FROM ExplanatoryNoteItem e "
      + "WHERE e.id = :itemId AND e.project.academicWork.teacher.id = :teacherId")
  boolean existsByIdAndTeacherId(@Param("itemId") Long itemId, @Param("teacherId") Long teacherId);

}
