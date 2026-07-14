package com.github.stepwise.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.AcademicWorkDeadline;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.entity.WorkTemplateChapter;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.WorkTemplateRepository;
import com.github.stepwise.web.dto.CreateWorkDto;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        log.info("Creating new academic work, templateId: {}, groupId: {}, deadlines: {}",
                workTemplateId, groupId, deadlineDtos);

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found with id: " + groupId));
        WorkTemplate template = workTemplateRepository.findById(workTemplateId)
                .orElseThrow(() -> new NotFoundException("Work template not found with id: " + workTemplateId));

        validateDeadlinesCoverAllChapters(template, deadlineDtos);

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
        log.info("Fetching works for group with id: {}", groupId);
        return academicWorkRepository.findByGroupId(groupId);
    }

    public List<AcademicWork> getByTeacherAndGroupId(Long teacherId, Long groupId) {
        log.info("Fetching works for teacher with id: {}, group with id: {}", teacherId, groupId);
        return groupId != null
                ? academicWorkRepository.findByGroupIdAndTeacherId(groupId, teacherId)
                : academicWorkRepository.findByTeacherId(teacherId);
    }

    public AcademicWork getById(Long workId) {
        log.info("Fetching work with id: {}", workId);
        return academicWorkRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("Work not found with id: " + workId));
    }

    public List<AcademicWork> getByStudentId(Long studentId) {
        log.info("Fetching works for student with id: {}", studentId);

        if (!userRepository.existsById(studentId)) {
            throw new NotFoundException("Student not found with id: " + studentId);
        }

        return academicWorkRepository.findByStudentId(studentId);
    }

    public List<AcademicWork> getWorksForRequester(String requestedStudentId, Long principalId,
            UserRole principalRole) {
        Long studentId = resolveStudentId(requestedStudentId, principalId, principalRole);
        return getByStudentId(studentId);
    }

    private Long resolveStudentId(String requestedStudentId, Long principalId, UserRole principalRole) {
        Long studentId = requestedStudentId == null ? principalId : Long.valueOf(requestedStudentId);

        if (principalRole == UserRole.STUDENT && !studentId.equals(principalId)) {
            log.warn("Student {} attempted to access works of student {}", principalId, studentId);
            throw new AccessDeniedException("Students can only view their own works");
        }

        if (principalRole != UserRole.STUDENT && requestedStudentId == null) {
            throw new IllegalArgumentException("Student ID must be provided");
        }

        return studentId;
    }

    private void validateDeadlinesCoverAllChapters(WorkTemplate template,
            List<CreateWorkDto.ChapterDeadlineDto> deadlineDtos) {
        Set<Integer> templateIndexes = template.getWorkTemplateChapters().stream()
                .map(WorkTemplateChapter::getIndexOfChapter)
                .collect(Collectors.toSet());
        Set<Integer> providedIndexes = deadlineDtos.stream()
                .map(CreateWorkDto.ChapterDeadlineDto::getChapterIndex)
                .collect(Collectors.toSet());

        if (!providedIndexes.containsAll(templateIndexes)) {
            throw new IllegalArgumentException("Deadlines must be provided for all chapters");
        }
    }

}
