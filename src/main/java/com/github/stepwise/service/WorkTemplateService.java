package com.github.stepwise.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.github.stepwise.entity.User;
import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.entity.WorkTemplateChapter;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.WorkTemplateRepository;
import com.github.stepwise.web.dto.CreateWorkTemplateDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkTemplateService {

    private final WorkTemplateRepository workTemplateRepository;

    private final UserRepository userRepository;

    public WorkTemplate create(CreateWorkTemplateDto dto) {
        log.info("Creating work template with title: {}", dto.getTemplateTitle());

        User teacher = userRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with id: " + dto.getTeacherId()));

        List<WorkTemplateChapter> chapters = dto.getChapters().stream()
                .map(chapterDto -> new WorkTemplateChapter(
                        chapterDto.getTitle(),
                        chapterDto.getIndex(),
                        chapterDto.getDescription(),
                        null,
                        chapterDto.getDeadline()))
                .collect(Collectors.toList());

        WorkTemplate workTemplate = WorkTemplate.builder()
                .templateTitle(dto.getTemplateTitle())
                .templateDescription(dto.getTemplateDescription())
                .workTitle(dto.getWorkTitle())
                .workDescription(dto.getWorkDescription())
                .countOfChapters(dto.getChapters().size())
                .type(dto.getType())
                .teacher(teacher)
                .workTemplateChapters(chapters)
                .build();

        return workTemplateRepository.save(workTemplate);
    }

}
