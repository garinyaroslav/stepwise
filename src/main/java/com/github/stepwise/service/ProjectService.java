package com.github.stepwise.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.github.stepwise.entity.ItemStatus;
import com.github.stepwise.entity.Project;
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

    @Value("${spring.mail.username}")
    private String fromEmail;

    public Project updateProject(Project newProject) {
        log.info("Updating project with id: {}, {}", newProject.getId(), newProject);

        Project project = projectRepository.findById(newProject.getId()).orElseThrow(() -> {
            log.error("Project with id {} not found", newProject.getId());
            return new IllegalArgumentException("Project not found with id: " + newProject.getId());
        });

        project.setTitle(newProject.getTitle());
        project.setDescription(newProject.getDescription());

        Project updatedProject = projectRepository.save(project);

        return updatedProject;
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
        int workItemsCount = project.getAcademicWork().getAcademicWorkChapters().size();

        if (approvedItemsCount != workItemsCount) {
            log.error("Cannot approve project with id: {}. Not all items are approved.", projectId);
            throw new IllegalArgumentException("Cannot approve project with id: " + projectId);
        }

        project.setApprovedForDefense(true);
        project.setApprovedForDefenseAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);

        log.info("Project with id: {} approved successfully", updatedProject.getId());

        try {
            User user = userRepository.findById(updatedProject.getStudent().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner of the project is not found"));

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setFrom(fromEmail);
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
