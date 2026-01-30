package com.github.stepwise.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.github.stepwise.entity.User;
import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.entity.WorkTemplateChapter;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.WorkTemplateRepository;
import com.github.stepwise.web.dto.CreateWorkTemplateDto;
import com.github.stepwise.web.dto.UpdateWorkTemplateDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkTemplateService {

    private final WorkTemplateRepository workTemplateRepository;

    private final UserRepository userRepository;

    public Page<WorkTemplate> findAllWithSearch(Pageable pageable, String search) {
        log.info("Fetching work templates with search: {}", search);

        if (search == null || search.isBlank())
            return workTemplateRepository.findAll(pageable);

        return workTemplateRepository.findAllWithSearch(pageable, search);
    }

    public WorkTemplate create(CreateWorkTemplateDto dto) {
        log.info("Creating work template with title: {}", dto.getTemplateTitle());

        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + dto.getTeacherId()));

        WorkTemplate workTemplate = WorkTemplate.builder()
                .templateTitle(dto.getTemplateTitle())
                .templateDescription(dto.getTemplateDescription())
                .workTitle(dto.getWorkTitle())
                .workDescription(dto.getWorkDescription())
                .countOfChapters(dto.getChapters().size())
                .type(dto.getType())
                .teacher(teacher)
                .build();

        List<WorkTemplateChapter> chapters = dto.getChapters().stream()
                .map(chapterDto -> chapterDto.toEntity(workTemplate))
                .collect(Collectors.toList());

        workTemplate.setWorkTemplateChapters(chapters);

        return workTemplateRepository.save(workTemplate);
    }

    public WorkTemplate update(Long id, UpdateWorkTemplateDto dto) {
        log.info("Updating Template with id: {}", id);

        WorkTemplate t = workTemplateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work template not found with id: " + id));

        if (dto.getTemplateTitle() != null)
            t.setTemplateTitle(dto.getTemplateTitle());
        if (dto.getTemplateDescription() != null)
            t.setTemplateDescription(dto.getTemplateDescription());
        if (dto.getWorkTitle() != null)
            t.setWorkTitle(dto.getWorkTitle());
        if (dto.getWorkDescription() != null)
            t.setWorkDescription(dto.getWorkDescription());
        if (dto.getType() != null)
            t.setType(dto.getType());
        if (dto.getChapters() != null && !dto.getChapters().isEmpty()) {
            List<WorkTemplateChapter> chapters = dto.getChapters().stream()
                    .map(chapterDto -> chapterDto.toEntity(t))
                    .collect(Collectors.toList());
            t.setWorkTemplateChapters(chapters);
            t.setCountOfChapters(chapters.size());
        }

        return workTemplateRepository.save(t);
    }

    public void delete(Long id) {
        log.info("Deleting work template with id: {}", id);

        workTemplateRepository.deleteById(id);
    }

}
