package com.github.stepwise.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.stepwise.entity.AcademicWork;

public interface AcademicWorkRepository extends JpaRepository<AcademicWork, Long> {

    List<AcademicWork> findByGroupId(Long groupId);

    @Query("SELECT aw FROM AcademicWork aw JOIN aw.group g JOIN g.students s WHERE s.id = :studentId")
    List<AcademicWork> findByStudentId(@Param("studentId") Long studentId);

    List<AcademicWork> findByTeacherId(Long teacherId);

    List<AcademicWork> findByGroupIdAndTeacherId(Long groupId, Long teacherId);

}
