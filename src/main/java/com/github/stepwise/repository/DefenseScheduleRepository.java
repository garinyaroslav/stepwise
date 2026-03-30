package com.github.stepwise.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.stepwise.entity.DefenseSchedule;

public interface DefenseScheduleRepository extends JpaRepository<DefenseSchedule, Long> {

    List<DefenseSchedule> findByAcademicWorkId(Long academicWorkId);

    @Query("SELECT COUNT(r) FROM DefenseRegistration r WHERE r.defenseSchedule.id = :scheduleId")
    int countRegistrations(@Param("scheduleId") Long scheduleId);

}
