package com.github.stepwise.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.stepwise.entity.StudyGroup;
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

    var usersList = userRepository.findAllById(studentsIds);

    studyGroupRepository.save(new StudyGroup(name, usersList));

    log.info("Group created with name: {}", name);
  }

}
