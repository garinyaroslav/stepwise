package com.github.stepwise.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.github.stepwise.configuration.MailConfigurationProperties;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.ProjectStatus;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final JavaMailSender mailSender;

    private final MailConfigurationProperties mailConfig;

    public Project updateProject(Project newProject) {
        log.info("Updating project with id: {}", newProject.getId());

        Project project = getByIdOrThrow(newProject.getId());
        project.setTitle(newProject.getTitle());
        project.setDescription(newProject.getDescription());

        return projectRepository.save(project);
    }

    public Project getByProjectId(Long projectId) {
        log.info("Fetching project id: {}", projectId);
        return getByIdOrThrow(projectId);
    }

    public List<Project> getAllByWorkId(Long workId) {
        log.info("Fetching all projects for work id: {}", workId);
        return projectRepository.findAllByAcademicWorkId(workId);
    }

    public List<Project> getAllByWorkIdAndStudentId(Long workId, Long studentId) {
        log.info("Fetching all projects for work id: {}, student id: {}", workId, studentId);
        return projectRepository.findAllByAcademicWorkIdAndStudentId(workId, studentId);
    }

    public List<Project> getForRequester(Long workId, Long principalId, UserRole principalRole) {
        return principalRole == UserRole.STUDENT
                ? getAllByWorkIdAndStudentId(workId, principalId)
                : getAllByWorkId(workId);
    }

    public Project approve(Long projectId) {
        log.info("Approving project with id: {}", projectId);

        Project project = getByIdOrThrow(projectId);

        int approvedItemsCount = (int) project.getItems().stream()
                .filter(item -> item.getStatus() == ItemStatus.APPROVED).count();
        int workItemsCount = project.getAcademicWork().getWorkTemplate().getWorkTemplateChapters().size();

        if (approvedItemsCount != workItemsCount) {
            log.error("Cannot approve project with id: {}. Not all items are approved.", projectId);
            throw new IllegalArgumentException("Cannot approve project with id: " + projectId);
        }

        project.setStatus(ProjectStatus.APPROVED_FOR_DEFENSE);
        project.setApprovedForDefenseAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(project);

        log.info("Project with id: {} approved successfully", updatedProject.getId());

        sendNotificationEmail(updatedProject, "Ваша работа одобрена для защиты",
                "Ваша работа на тему: \"%s\" допущена к защите. Пожалуйста, свяжитесь с вашим научным руководителем для дальнейших инструкций."
                        .formatted(updatedProject.getTitle()));

        return updatedProject;
    }

    public Project defend(Long projectId, Integer grade) {
        log.info("Marking project id: {} as defended with grade: {}", projectId, grade);

        if (grade < 1 || grade > 5) {
            throw new IllegalArgumentException("Grade must be between 1 and 5");
        }

        Project project = getByIdOrThrow(projectId);

        if (project.getStatus() != ProjectStatus.APPROVED_FOR_DEFENSE) {
            log.error("Cannot mark project id: {} as defended, current status: {}", projectId, project.getStatus());
            throw new IllegalStateException("Project must be in APPROVED_FOR_DEFENSE status to be marked as defended");
        }

        project.setStatus(ProjectStatus.DEFENDED);
        project.setGrade(grade);
        project.setDefendedAt(LocalDateTime.now());
        Project updatedProject = projectRepository.save(project);

        log.info("Project id: {} marked as defended with grade: {}", projectId, grade);

        sendNotificationEmail(updatedProject, "Результат защиты вашей работы",
                "Ваша работа на тему: \"%s\" успешно защищена. Итоговая оценка: %d."
                        .formatted(updatedProject.getTitle(), grade));

        return updatedProject;
    }

    public boolean isProjectBelongsToStudent(Long projectId, Long studentId) {
        log.info("Checking if project with id: {} belongs to student with id: {}", projectId, studentId);
        return projectRepository.existsByIdAndStudentId(projectId, studentId);
    }

    public boolean isProjectBelongsToTeacher(Long projectId, Long teacherId) {
        log.info("Checking if project with id: {} belongs to teacher with id: {}", projectId, teacherId);
        return projectRepository.existsByIdAndTeacherId(projectId, teacherId);
    }

    public Project getForStudent(Long projectId, Long studentId) {
        if (!isProjectBelongsToStudent(projectId, studentId)) {
            log.warn("Project {} does not belong to student {}", projectId, studentId);
            throw new NotFoundException("Project not found with id: " + projectId);
        }
        return getByIdOrThrow(projectId);
    }

    public Project approveAsTeacher(Long projectId, Long teacherId) {
        assertBelongsToTeacher(projectId, teacherId);
        return approve(projectId);
    }

    public Project defendAsTeacher(Long projectId, Long teacherId, Integer grade) {
        assertBelongsToTeacher(projectId, teacherId);
        return defend(projectId, grade);
    }

    private void assertBelongsToTeacher(Long projectId, Long teacherId) {
        if (!isProjectBelongsToTeacher(projectId, teacherId)) {
            log.warn("Project {} does not belong to teacher {}", projectId, teacherId);
            throw new NotFoundException("Project not found with id: " + projectId);
        }
    }

    private Project getByIdOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new NotFoundException("Project not found with id: " + projectId));
    }

    private void sendNotificationEmail(Project project, String subject, String body) {
        try {
            User user = userRepository.findById(project.getStudent().getId())
                    .orElseThrow(() -> new NotFoundException("Owner of the project not found"));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setFrom(mailConfig.getUsername());
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            log.info("Email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email for project id: {}", project.getId(), e);
        }
    }

}
