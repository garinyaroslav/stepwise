package com.github.stepwise.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.WorkTemplateRepository;
import com.github.stepwise.web.dto.CreateWorkDto;

import jakarta.transaction.Transactional;
import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.AcademicWorkDeadline;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.entity.WorkTemplateChapter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public void create(Long workTemplateId, Long groupId, List<CreateWorkDto.ChapterDeadlineDto> deadlineDtos) {
        log.info("Creating new academic work, templateId: {}, groupId: {}, deadlines: {}", workTemplateId, groupId,
                deadlineDtos);

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        WorkTemplate template = workTemplateRepository.findById(workTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + workTemplateId));

        Set<Integer> templateIndexes = template.getWorkTemplateChapters().stream()
                .map(WorkTemplateChapter::getIndexOfChapter)
                .collect(Collectors.toSet());
        Set<Integer> providedIndexes = deadlineDtos.stream()
                .map(CreateWorkDto.ChapterDeadlineDto::getChapterIndex)
                .collect(Collectors.toSet());

        if (!providedIndexes.containsAll(templateIndexes)) {
            throw new IllegalArgumentException("Deadlines must be provided for all chapters");
        }

        AcademicWork academicWork = AcademicWork.builder()
                .group(group)
                .workTemplate(template)
                .build();

        List<AcademicWorkDeadline> deadlines = deadlineDtos.stream()
                .map(dto -> AcademicWorkDeadline.builder()
                        .academicWork(academicWork)
                        .indexOfChapter(dto.getChapterIndex())
                        .deadline(dto.getDeadline())
                        .build())
                .collect(Collectors.toList());
        academicWork.setDeadlines(deadlines);

        academicWorkRepository.save(academicWork);

        List<Project> projects = group.getStudents().stream()
                .map(student -> new Project(
                        "Мой проект по теме: " + template.getWorkTitle(),
                        "Моё описание проекта",
                        student, academicWork))
                .collect(Collectors.toList());
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
