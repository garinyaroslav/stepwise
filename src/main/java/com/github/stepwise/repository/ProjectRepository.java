package com.github.stepwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByAcademicWorkId(Long academicWorkId);

    List<Project> findAllByAcademicWorkIdAndStudentId(Long academicWorkId, Long studentId);

    boolean existsByIdAndStudentId(Long projectId, Long studentId);

    @Query("SELECT CASE WHEN count(p) > 0 THEN true ELSE false END FROM Project p JOIN p.academicWork aw JOIN aw.workTemplate wt WHERE p.id = :projectId AND wt.teacher.id = :teacherId")
    boolean existsByIdAndTeacherId(@Param("projectId") Long projectId, @Param("teacherId") Long teacherId);

    List<Project> findByAcademicWorkInAndStudentIdIn(List<AcademicWork> academicWorks, List<Long> studentIds);

    List<Project> findByAcademicWorkIdInAndStudentIdIn(List<Long> academicWorkIds, List<Long> studentIds);

}
