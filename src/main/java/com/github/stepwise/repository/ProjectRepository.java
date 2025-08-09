package com.github.stepwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.github.stepwise.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  List<Project> findAllByAcademicWorkId(Long academicWorkId);

  List<Project> findAllByAcademicWorkIdAndStudentId(Long academicWorkId, Long studentId);

  boolean existsByIdAndStudentId(Long projectId, Long studentId);

  @Query("SELECT CASE WHEN count(p) > 0 THEN true ELSE false END FROM Project p JOIN p.academicWork aw WHERE p.id = :projectId AND aw.teacher.id = :teacherId")
  boolean existsByIdAndTeacherId(Long projectId, Long teacherId);

}
