package com.github.stepwise.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.github.stepwise.entity.Profile;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.UserRole;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.StudyGroupRepository;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.web.dto.ProfileDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final StudyGroupRepository studyGroupRepository;

    public User findById(Long id) {
        log.info("Fetching user by id: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    public Page<User> getStudents(String search, Pageable pageable) {
        log.info("Fetching students, search={}, pageable={}", search, pageable);
        return StringUtils.hasText(search)
                ? userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.STUDENT, pageable)
                : userRepository.findByRole(UserRole.STUDENT, pageable);
    }

    public Page<User> getTeachers(String search, Pageable pageable) {
        log.info("Fetching teachers, search={}, pageable={}", search, pageable);
        return StringUtils.hasText(search)
                ? userRepository.findByUsernameOrFirstNameOrLastName(search, UserRole.TEACHER, pageable)
                : userRepository.findByRole(UserRole.TEACHER, pageable);
    }

    public List<User> getStudentsByGroupId(Long groupId) {
        log.info("Fetching students by groupId: {}", groupId);
        assertGroupExists(groupId);
        return userRepository.findAllByGroupId(groupId);
    }

    public List<User> getUsersWithTempPasswordByGroupId(Long groupId) {
        log.info("Fetching users with temp password by groupId: {}", groupId);
        assertGroupExists(groupId);
        return userRepository.findAllByGroupIdAndTempPasswordIsNotNull(groupId);
    }

    @Transactional
    public User updateProfile(Long userId, ProfileDto dto) {
        log.info("Updating profile for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Profile profile = user.getProfile();
        applyIfPresent(dto.getFirstName(), profile::setFirstName);
        applyIfPresent(dto.getLastName(), profile::setLastName);
        applyIfPresent(dto.getMiddleName(), profile::setMiddleName);
        applyIfPresent(dto.getPhoneNumber(), profile::setPhoneNumber);
        applyIfPresent(dto.getAddress(), profile::setAddress);

        return userRepository.save(user);
    }

    public Long resolveProfileTargetId(ProfileDto dto, Long requesterId, UserRole requesterRole) {
        if (requesterRole != UserRole.ADMIN) {
            return requesterId;
        }
        if (dto.getId() == null) {
            throw new IllegalArgumentException("Profile ID must not be null for admin");
        }
        return dto.getId();
    }

    private void assertGroupExists(Long groupId) {
        if (!studyGroupRepository.existsById(groupId)) {
            throw new NotFoundException("Group not found with id: " + groupId);
        }
    }

    private void applyIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
