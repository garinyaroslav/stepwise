package com.github.stepwise.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import jakarta.transaction.Transactional;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.AcademicWorkChapter;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicWorkService {

  private final AcademicWorkRepository academicWorkRepository;

  private final StudyGroupRepository studyGroupRepository;

  private final UserRepository userRepository;

  private final ProjectRepository projectRepository;

  @Transactional
  public void create(AcademicWork academicWork, List<AcademicWorkChapter> chapters, Long groupId,
      Long teacherId) {
    log.info("Creating work: {}, chapters: {}, groupId: {}, teacherId: {}", academicWork, chapters,
        groupId, teacherId);

    StudyGroup group = studyGroupRepository.findById(groupId)
        .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));

    User teacher = userRepository.findById(teacherId)
        .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + teacherId));

    academicWork.setGroup(group);
    academicWork.setTeacher(teacher);
    academicWork.setAcademicWorkChapters(chapters);

    academicWorkRepository.save(academicWork);

    List<Project> projects = new LinkedList<>();

    for (User student : group.getStudents()) {
      projects.add(new Project("Мой проект по теме: " + academicWork.getTitle(), "Описание проекта",
          student, academicWork));
    }

    projectRepository.saveAll(projects);
  }

}
