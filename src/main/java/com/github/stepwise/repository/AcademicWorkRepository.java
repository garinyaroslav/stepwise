package com.github.stepwise.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.AcademicWork;

public interface AcademicWorkRepository extends JpaRepository<AcademicWork, Long> {

  List<AcademicWork> findByGroupId(Long groupId);

}
