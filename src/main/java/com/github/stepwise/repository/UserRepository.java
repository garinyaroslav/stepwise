package com.github.stepwise.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  List<User> findByIdInAndRole(Collection<Long> ids, UserRole role);

  Page<User> findByRole(UserRole role, Pageable pageable);

  @Query("SELECT u FROM User u LEFT JOIN u.profile p WHERE " + "u.role = :role AND ("
      + "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR "
      + "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR "
      + "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<User> findByUsernameOrFirstNameOrLastName(@Param("search") String search,
      @Param("role") UserRole role, Pageable pageable);

  Optional<User> findByEmail(String email);
}
