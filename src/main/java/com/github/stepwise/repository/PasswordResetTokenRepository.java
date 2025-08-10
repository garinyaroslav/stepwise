package com.github.stepwise.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.stepwise.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
  Optional<PasswordResetToken> findByToken(String token);
}
