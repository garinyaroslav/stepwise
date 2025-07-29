package com.github.stepwise.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  List<User> findByIdInAndRole(Collection<Long> ids, UserRole role);

  Page<User> findByRole(UserRole role, Pageable pageable);
}
