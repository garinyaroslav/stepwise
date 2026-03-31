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
        log.info("Updating project with id: {}, {}", newProject.getId(), newProject);

        Project project = projectRepository.findById(newProject.getId()).orElseThrow(() -> {
            log.error("Project with id {} not found", newProject.getId());
            return new IllegalArgumentException("Project not found with id: " + newProject.getId());
        });

        project.setTitle(newProject.getTitle());
        project.setDescription(newProject.getDescription());

        return projectRepository.save(project);
    }

    public Project getByProjectId(Long projectId) {
        log.info("Fetching project id: {}", projectId);

        return projectRepository.findById(projectId).orElseThrow(
                () -> new IllegalArgumentException("Project not found project id: " + projectId));
    }

    public List<Project> getAllByWorkId(Long workId) {
        log.info("Fetching all projects for work id: {}", workId);

        return projectRepository.findAllByAcademicWorkId(workId);
    }

    public List<Project> getAllByWorkIdAndStudentId(Long workId, Long studentId) {
        log.info("Fetching all projects for work id: {}, and student id: {}", workId, studentId);

        return projectRepository.findAllByAcademicWorkIdAndStudentId(workId, studentId);
    }

    public Project approve(Long projectId) {
        log.info("Approving project with id: {}", projectId);

        Project project = projectRepository.findById(projectId).orElseThrow(() -> {
            log.error("Project with id {} not found", projectId);
            return new IllegalArgumentException("Project not found with id: " + projectId);
        });

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

        try {
            User user = userRepository.findById(updatedProject.getStudent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner of the project is not found"));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setFrom(mailConfig.getUsername());
            message.setSubject("Ваша работа одобрена для защиты");

            message.setText(String.format(
                    "Ваша работа на тему: \"%s\" допущена к защите. Пожалуйста, свяжитесь с вашим научным руководителем для дальнейших инструкций.",
                    updatedProject.getTitle()));
            mailSender.send(message);

            log.info("Email was sended on this email {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send approval email for project id: {}", projectId, e);
        }

        return updatedProject;
    }

    public Project defend(Long projectId, Integer grade) {
        log.info("Marking project id: {} as defended with grade: {}", projectId, grade);

        if (grade < 1 || grade > 5) {
            throw new IllegalArgumentException("Grade must be between 1 and 5");
        }

        Project project = projectRepository.findById(projectId).orElseThrow(() -> {
            log.error("Project with id {} not found", projectId);
            return new IllegalArgumentException("Project not found with id: " + projectId);
        });

        if (project.getStatus() != ProjectStatus.APPROVED_FOR_DEFENSE) {
            log.error("Cannot mark project id: {} as defended, current status: {}", projectId, project.getStatus());
            throw new IllegalStateException(
                    "Project must be in APPROVED_FOR_DEFENSE status to be marked as defended");
        }

        project.setStatus(ProjectStatus.DEFENDED);
        project.setGrade(grade);
        project.setDefendedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);
        log.info("Project id: {} marked as defended with grade: {}", projectId, grade);

        try {
            User user = userRepository.findById(updatedProject.getStudent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner of the project is not found"));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setFrom(mailConfig.getUsername());
            message.setSubject("Результат защиты вашей работы");
            message.setText(String.format(
                    "Ваша работа на тему: \"%s\" успешно защищена. Итоговая оценка: %d.",
                    updatedProject.getTitle(), grade));
            mailSender.send(message);

            log.info("Defense result email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send defense result email for project id: {}", projectId, e);
        }

        return updatedProject;
    }

    public boolean isProjectBelongsToStudent(Long projectId, Long studentId) {
        log.info("Checking if project with id: {} belongs to student with id: {}", projectId,
                studentId);

        return projectRepository.existsByIdAndStudentId(projectId, studentId);
    }

    public boolean isProjectBelongsToTeacher(Long projectId, Long teacherId) {
        log.info("Checking if project with id: {} belongs to teacher with id: {}", projectId,
                teacherId);

        return projectRepository.existsByIdAndTeacherId(projectId, teacherId);
    }

}
