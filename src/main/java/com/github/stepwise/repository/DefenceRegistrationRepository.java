package com.github.stepwise.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.stepwise.entity.DefenseRegistration;

public interface DefenceRegistrationRepository extends JpaRepository<DefenseRegistration, Long> {

    @Query("""
            SELECT COUNT(r) > 0 FROM DefenseRegistration r
            WHERE r.project.student.id = :studentId
            AND r.defenseSchedule.academicWork.id = :academicWorkId
            """)
    boolean existsByStudentIdAndAcademicWorkId(@Param("studentId") Long studentId,
            @Param("academicWorkId") Long academicWorkId);

    @Query("""
            SELECT r FROM DefenseRegistration r
            WHERE r.project.student.id = :studentId
            AND r.defenseSchedule.academicWork.id = :academicWorkId
            """)
    Optional<DefenseRegistration> findByStudentIdAndAcademicWorkId(@Param("studentId") Long studentId,
            @Param("academicWorkId") Long academicWorkId);

}
