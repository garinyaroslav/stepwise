package com.github.stepwise.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.role = :role")
  List<User> findAllUsersByRole(@Param("ids") Collection<Long> ids, @Param("role") UserRole role);
}
