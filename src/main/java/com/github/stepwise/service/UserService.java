package com.github.stepwise.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.stepwise.entity.Profile;
import com.github.stepwise.entity.StudyGroup;
import com.github.stepwise.entity.User;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.stepwise.entity.UserRole;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final StudyGroupRepository studyGroupRepository;

    public User findById(Long id) {
        log.info("Fetching user by id: {}", id);

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public Page<User> getAllStudents(Pageable pageable) {
        log.info("Fetching all students by pageable: {}", pageable);

        return userRepository.findByRole(UserRole.STUDENT, pageable);
    }

    public Page<User> getAllStudents(String search, Pageable pageable) {
        log.info("Fetching all students by search: {} and pageable: {}", search, pageable);

        return userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.STUDENT, pageable);
    }

    public Page<User> getAllTeachers(Pageable pageable) {
        log.info("Fetching all teachers by pageable: {}", pageable);

        return userRepository.findByRole(UserRole.TEACHER, pageable);
    }

    public Page<User> getAllTeachers(String search, Pageable pageable) {
        log.info("Fetching all teachers by search: {} and pageable: {}", search, pageable);

        return userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.TEACHER, pageable);
    }

    @Transactional
    public List<User> getAllStudentsByGroupId(Long groupId) {
        log.info("Fetching all students by groupId: {}", groupId);

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));

        List<Long> studnetIds = group.getStudents().stream().map(User::getId).toList();

        return userRepository.findAllById(studnetIds);
    }

    public List<User> getUsersWithTempPasswordByGroupId(Long groupId) {
        log.info("Fetching users with temporary passwords by groupId: {}", groupId);

        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + groupId));

        return group.getStudents().stream()
                .filter(user -> Boolean.TRUE.equals(user.getIsTempPassword()))
                .toList();
    }

    public User updateProfile(Long userId, String firstName, String lastName, String middleName,
            String phoneNumber, String address) {
        log.info("Updating profile for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Profile p = user.getProfile();

        if (firstName != null)
            p.setFirstName(firstName);
        if (lastName != null)
            p.setLastName(lastName);
        if (middleName != null)
            p.setMiddleName(middleName);
        if (phoneNumber != null)
            p.setPhoneNumber(phoneNumber);
        if (address != null)
            p.setAddress(address);

        return userRepository.save(user);
    }

}
