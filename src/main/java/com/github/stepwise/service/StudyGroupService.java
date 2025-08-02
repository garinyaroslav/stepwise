package com.github.stepwise.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyGroupService {

  private final StudyGroupRepository studyGroupRepository;
  private final UserRepository userRepository;

  @Transactional
  public void create(String name, List<Long> studentsIds) {
    log.info("Create group with name: {}", name);

    var usersList = userRepository.findByIdInAndRole(studentsIds, UserRole.STUDENT);

    if (usersList.isEmpty()) {
      log.warn("No users found with provided IDs: {}", studentsIds);
      return;
    }

    studyGroupRepository.save(new StudyGroup(name, usersList));

    log.info("Group created with name: {}", name);
  }

  @Transactional
  public StudyGroup update(Long id, List<Long> newStudentsIds) {
    log.info("Update group with id: {}", id);

    StudyGroup studyGroup = studyGroupRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Group with id " + id + " not found"));

    var newUsersList = userRepository.findByIdInAndRole(newStudentsIds, UserRole.STUDENT);

    if (newUsersList.isEmpty()) {
      log.warn("No users found with provided IDs: {}", newStudentsIds);
      throw new IllegalArgumentException("No valid students found for the provided IDs");
    }

    studyGroup.setStudents(newUsersList);
    StudyGroup updatedStudyGroup = studyGroupRepository.save(studyGroup);

    log.info("Group updated with id: {}", id);

    return updatedStudyGroup;
  }
}
