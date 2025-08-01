package com.github.stepwise.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.stepwise.entity.UserRole;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  private final StudyGroupRepository studyGroupRepository;

  public Page<User> getAllStudents(Pageable pageable) {
    log.info("Fetching all students by pageable: {}", pageable);

    return userRepository.findByRole(UserRole.STUDENT, pageable);
  }

  @Transactional
  public List<User> getAllStudentsByGroupId(Long groupId) {
    log.info("Fetching all students by groupId: {}", groupId);

    StudyGroup group = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));

    List<Long> studnetIds = group.getStudents().stream().map(User::getId).toList();

    return userRepository.findAllById(studnetIds);
  }


}
