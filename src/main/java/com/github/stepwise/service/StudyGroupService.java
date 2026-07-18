package com.github.stepwise.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.web.dto.GroupResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;

    private final UserRepository userRepository;

    private final AcademicWorkRepository academicWorkRepository;

    private final ProjectRepository projectRepository;

    public List<GroupResponseDto> findAllSummaries(String search) {
        log.info("Fetching all groups, search={}", search);
        return StringUtils.hasText(search)
                ? studyGroupRepository.findSummariesByNameContaining(search)
                : studyGroupRepository.findAllSummaries();
    }

    public StudyGroup findById(Long groupId) {
        log.info("Fetching group by id: {}", groupId);
        return studyGroupRepository.findByIdWithStudents(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found with id: " + groupId));
    }

    @Transactional
    public void create(String name, List<Long> studentIds) {
        log.info("Creating group with name: {}", name);

        List<User> students = userRepository.findByIdInAndRole(studentIds, UserRole.STUDENT);
        if (students.isEmpty()) {
            log.warn("No users found with provided IDs: {}", studentIds);
        }

        studyGroupRepository.save(new StudyGroup(name, students));
        log.info("Group created with name: {}", name);
    }

    @Transactional
    public StudyGroup update(Long id, List<Long> newStudentIds) {
        log.info("Updating group with id: {}", id);

        StudyGroup studyGroup = studyGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found with id: " + id));

        List<User> newUsersList = userRepository.findByIdInAndRole(newStudentIds, UserRole.STUDENT);
        if (newUsersList.isEmpty()) {
            log.warn("No users found with provided IDs: {}", newStudentIds);
            throw new IllegalArgumentException("No valid students found for the provided IDs");
        }

        List<User> oldUsersList = studyGroup.getStudents();
        Set<Long> oldUserIds = oldUsersList.stream().map(User::getId).collect(Collectors.toSet());
        Set<Long> newUserIds = newUsersList.stream().map(User::getId).collect(Collectors.toSet());

        List<User> removedUsers = oldUsersList.stream()
                .filter(user -> !newUserIds.contains(user.getId()))
                .toList();
        List<User> addedUsers = newUsersList.stream()
                .filter(user -> !oldUserIds.contains(user.getId()))
                .toList();

        List<AcademicWork> works = academicWorkRepository.findByGroupId(id);

        removeProjectsForRemovedStudents(removedUsers, works);
        addProjectsForNewStudents(addedUsers, works);

        studyGroup.setStudents(newUsersList);
        StudyGroup updatedStudyGroup = studyGroupRepository.save(studyGroup);

        log.info("Group updated with id: {}", id);
        return updatedStudyGroup;
    }

    private void removeProjectsForRemovedStudents(List<User> removedUsers, List<AcademicWork> works) {
        if (removedUsers.isEmpty() || works.isEmpty()) {
            return;
        }

        List<Long> removedUserIds = removedUsers.stream().map(User::getId).toList();
        List<Project> projectsToRemove = projectRepository.findByAcademicWorkInAndStudentIdIn(works, removedUserIds);

        if (!projectsToRemove.isEmpty()) {
            log.info("Removing {} projects for removed students", projectsToRemove.size());
            projectRepository.deleteAll(projectsToRemove);
        }
    }

    private void addProjectsForNewStudents(List<User> addedUsers, List<AcademicWork> works) {
        if (addedUsers.isEmpty() || works.isEmpty()) {
            return;
        }

        List<Project> projectsToAdd = new ArrayList<>();
        for (User student : addedUsers) {
            for (AcademicWork work : works) {
                projectsToAdd.add(new Project(
                        "Мой проект по теме: " + work.getWorkTemplate().getWorkTitle(),
                        "Моё описание проекта", student, work));
            }
        }

        log.info("Adding {} projects for new students", projectsToAdd.size());
        projectRepository.saveAll(projectsToAdd);
    }

}
