package com.github.stepwise.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.WorkTemplateRepository;

import jakarta.transaction.Transactional;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.WorkTemplate;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AcademicWorkService {

    private final AcademicWorkRepository academicWorkRepository;

    private final StudyGroupRepository studyGroupRepository;

    private final UserRepository userRepository;

    private final ProjectRepository projectRepository;

    private final WorkTemplateRepository workTemplateRepository;

    @Transactional
    public void create(Long workTemplateId, Long groupId) {
        log.info("Creating work by template: {}, groupId: {}", workTemplateId, groupId);

        CompletableFuture<StudyGroup> groupFuture = CompletableFuture
                .supplyAsync(() -> studyGroupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId)));

        CompletableFuture<WorkTemplate> templateFuture = CompletableFuture.supplyAsync(() -> workTemplateRepository
                .findById(workTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Work template not found with id: " + workTemplateId)));

        StudyGroup group = groupFuture.join();
        WorkTemplate template = templateFuture.join();

        AcademicWork academicWork = AcademicWork.builder()
                .group(group)
                .workTemplate(template)
                .build();

        academicWorkRepository.save(academicWork);

        List<Project> projects = new LinkedList<>();

        for (User student : group.getStudents()) {
            projects.add(new Project("Мой проект по теме: " + template.getWorkTitle(), "Моё описание проекта",
                    student, academicWork));
        }

        projectRepository.saveAll(projects);
    }

    public List<AcademicWork> getByGroupId(Long groupId) {
        log.info("Getching works for group with id: {}", groupId);

        return academicWorkRepository.findByGroupId(groupId);
    }

    public List<AcademicWork> getByTeacherAndGroupId(Long teacherId, Long groupId) {
        log.info("Getching works for teacher with id: {}", teacherId);

        if (groupId != null)
            return academicWorkRepository.findByGroupIdAndTeacherId(groupId, teacherId);

        return academicWorkRepository.findByTeacherId(teacherId);
    }

    public AcademicWork getById(Long groupId) {
        log.info("Getching work with id: {}", groupId);

        AcademicWork work = academicWorkRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Work not found with id: " + groupId));

        return work;
    }

    public List<AcademicWork> getByStudentId(Long studentId) {
        log.info("Fetching works for student with id: {}", studentId);

        userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentId));

        return academicWorkRepository.findByStudentId(studentId);
    }

}
