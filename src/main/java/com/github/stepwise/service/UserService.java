package com.github.stepwise.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.github.stepwise.entity.User;
import com.github.stepwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.stepwise.entity.UserRole;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public Page<User> getAllStudents(Pageable pageable) {
    log.info("Fetching all students by pageable: {}", pageable);

    return userRepository.findByRole(UserRole.STUDENT, pageable);
  }


}
