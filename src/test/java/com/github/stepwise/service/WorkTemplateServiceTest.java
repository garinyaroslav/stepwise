package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.github.stepwise.entity.ProjectType;
import com.github.stepwise.entity.User;
import com.github.stepwise.entity.WorkTemplate;
import com.github.stepwise.entity.WorkTemplateChapter;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.UserRepository;
import com.github.stepwise.repository.WorkTemplateRepository;
import com.github.stepwise.web.dto.CreateWorkTemplateDto;
import com.github.stepwise.web.dto.UpdateWorkTemplateDto;
import com.github.stepwise.web.dto.WorkChapterDto;

@ExtendWith(MockitoExtension.class)
class WorkTemplateServiceTest {

    @Mock
    private WorkTemplateRepository workTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WorkTemplateService workTemplateService;

    private User teacher;
    private WorkTemplate template;

    @BeforeEach
    void setUp() {
        teacher = User.builder()
                .id(10L)
                .username("teacher1")
                .build();

        template = WorkTemplate.builder()
                .id(1L)
                .templateTitle("Template A")
                .templateDescription("Template A description")
                .workTitle("Work A")
                .workDescription("Work A description")
                .countOfChapters(2)
                .type(ProjectType.COURSEWORK)
                .teacher(teacher)
                .build();
    }

    @Test
    void findAllWithSearch_WithoutSearch_ShouldReturnAllTemplates() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WorkTemplate> page = new PageImpl<>(List.of(template), pageable, 1);
        when(workTemplateRepository.findAll(pageable)).thenReturn(page);

        Page<WorkTemplate> result = workTemplateService.findAllWithSearch(pageable, null);

        assertEquals(1, result.getTotalElements());
        verify(workTemplateRepository).findAll(pageable);
        verify(workTemplateRepository, never()).findAllWithSearch(any(), anyString());
    }

    @Test
    void findAllWithSearch_WithBlankSearch_ShouldReturnAllTemplates() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<WorkTemplate> page = new PageImpl<>(List.of(template), pageable, 1);
        when(workTemplateRepository.findAll(pageable)).thenReturn(page);

        Page<WorkTemplate> result = workTemplateService.findAllWithSearch(pageable, "   ");

        assertEquals(1, result.getTotalElements());
        verify(workTemplateRepository).findAll(pageable);
        verify(workTemplateRepository, never()).findAllWithSearch(any(), anyString());
    }

    @Test
    void findAllWithSearch_WithSearch_ShouldReturnFilteredTemplates() {
        String search = "Work A";
        Pageable pageable = PageRequest.of(0, 10);
        Page<WorkTemplate> page = new PageImpl<>(List.of(template), pageable, 1);
        when(workTemplateRepository.findAllWithSearch(pageable, search)).thenReturn(page);

        Page<WorkTemplate> result = workTemplateService.findAllWithSearch(pageable, search);

        assertEquals(1, result.getTotalElements());
        verify(workTemplateRepository).findAllWithSearch(pageable, search);
        verify(workTemplateRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void findById_WhenExists_ShouldReturnTemplate() {
        when(workTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        Optional<WorkTemplate> result = workTemplateService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Template A", result.get().getTemplateTitle());
        verify(workTemplateRepository).findById(1L);
    }

    @Test
    void findById_WhenNotExists_ShouldReturnEmpty() {
        when(workTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<WorkTemplate> result = workTemplateService.findById(999L);

        assertTrue(result.isEmpty());
        verify(workTemplateRepository).findById(999L);
    }

    @Test
    void getByIdOrThrow_WhenExists_ShouldReturnTemplate() {
        when(workTemplateRepository.findById(1L)).thenReturn(Optional.of(template));

        WorkTemplate result = workTemplateService.getByIdOrThrow(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(workTemplateRepository).findById(1L);
    }

    @Test
    void getByIdOrThrow_WhenNotExists_ShouldThrowNotFoundException() {
        when(workTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> workTemplateService.getByIdOrThrow(999L));

        assertEquals("Work template not found with id: 999", exception.getMessage());
        verify(workTemplateRepository).findById(999L);
    }

    @Test
    void create_WithValidTeacher_ShouldSaveTemplateWithChapters() {
        WorkChapterDto chapterDto1 = mock(WorkChapterDto.class);
        WorkChapterDto chapterDto2 = mock(WorkChapterDto.class);

        CreateWorkTemplateDto dto = new CreateWorkTemplateDto();
        dto.setTemplateTitle("New Template");
        dto.setTemplateDescription("New Template description");
        dto.setWorkTitle("New Work");
        dto.setWorkDescription("New Work description");
        dto.setType(ProjectType.COURSEWORK);
        dto.setTeacherId(10L);
        dto.setChapters(List.of(chapterDto1, chapterDto2));

        WorkTemplateChapter chapter1 = WorkTemplateChapter.builder().title("Ch1").indexOfChapter(1).build();
        WorkTemplateChapter chapter2 = WorkTemplateChapter.builder().title("Ch2").indexOfChapter(2).build();

        when(chapterDto1.toEntity(any(WorkTemplate.class))).thenReturn(chapter1);
        when(chapterDto2.toEntity(any(WorkTemplate.class))).thenReturn(chapter2);
        when(userRepository.findById(10L)).thenReturn(Optional.of(teacher));
        when(workTemplateRepository.save(any(WorkTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkTemplate result = workTemplateService.create(dto);

        assertNotNull(result);
        assertEquals("New Template", result.getTemplateTitle());
        assertEquals(2, result.getCountOfChapters());
        assertEquals(2, result.getWorkTemplateChapters().size());
        assertEquals(teacher, result.getTeacher());

        verify(userRepository).findById(10L);
        verify(workTemplateRepository).save(any(WorkTemplate.class));
    }

    @Test
    void create_WhenTeacherNotFound_ShouldThrowNotFoundException() {
        CreateWorkTemplateDto dto = new CreateWorkTemplateDto();
        dto.setTemplateTitle("New Template");
        dto.setChapters(List.of());
        dto.setTeacherId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> workTemplateService.create(dto));

        assertEquals("Teacher not found with id: 999", exception.getMessage());
        verify(workTemplateRepository, never()).save(any(WorkTemplate.class));
    }

    @Test
    void update_WhenExists_ShouldUpdateOnlyProvidedFields() {
        UpdateWorkTemplateDto dto = new UpdateWorkTemplateDto();
        dto.setTemplateTitle("Updated Title");
        dto.setWorkDescription("Updated Work description");

        when(workTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(workTemplateRepository.save(any(WorkTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkTemplate result = workTemplateService.update(1L, dto);

        assertEquals("Updated Title", result.getTemplateTitle());
        assertEquals("Updated Work description", result.getWorkDescription());
        assertEquals("Template A description", result.getTemplateDescription());
        assertEquals("Work A", result.getWorkTitle());
        assertEquals(ProjectType.COURSEWORK, result.getType());
        assertEquals(2, result.getCountOfChapters());

        verify(workTemplateRepository).findById(1L);
        verify(workTemplateRepository).save(template);
    }

    @Test
    void update_WhenNotExists_ShouldThrowNotFoundException() {
        UpdateWorkTemplateDto dto = new UpdateWorkTemplateDto();
        dto.setTemplateTitle("Updated Title");

        when(workTemplateRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> workTemplateService.update(999L, dto));

        assertEquals("Work template not found with id: 999", exception.getMessage());
        verify(workTemplateRepository, never()).save(any(WorkTemplate.class));
    }

    @Test
    void update_WithChapters_ShouldReplaceChaptersAndUpdateCount() {
        WorkChapterDto chapterDto1 = mock(WorkChapterDto.class);
        WorkChapterDto chapterDto2 = mock(WorkChapterDto.class);
        WorkChapterDto chapterDto3 = mock(WorkChapterDto.class);

        UpdateWorkTemplateDto dto = new UpdateWorkTemplateDto();
        dto.setChapters(List.of(chapterDto1, chapterDto2, chapterDto3));

        WorkTemplateChapter chapter1 = WorkTemplateChapter.builder().title("Ch1").indexOfChapter(1).build();
        WorkTemplateChapter chapter2 = WorkTemplateChapter.builder().title("Ch2").indexOfChapter(2).build();
        WorkTemplateChapter chapter3 = WorkTemplateChapter.builder().title("Ch3").indexOfChapter(3).build();

        when(chapterDto1.toEntity(template)).thenReturn(chapter1);
        when(chapterDto2.toEntity(template)).thenReturn(chapter2);
        when(chapterDto3.toEntity(template)).thenReturn(chapter3);

        when(workTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(workTemplateRepository.save(any(WorkTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkTemplate result = workTemplateService.update(1L, dto);

        assertEquals(3, result.getCountOfChapters());
        assertEquals(3, result.getWorkTemplateChapters().size());
        verify(workTemplateRepository).save(template);
    }

    @Test
    void update_WithEmptyChaptersList_ShouldNotReplaceChapters() {
        UpdateWorkTemplateDto dto = new UpdateWorkTemplateDto();
        dto.setChapters(List.of());

        when(workTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(workTemplateRepository.save(any(WorkTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkTemplate result = workTemplateService.update(1L, dto);

        assertEquals(2, result.getCountOfChapters());
        verify(workTemplateRepository).save(template);
    }

    @Test
    void update_WithAllNullFields_ShouldNotChangeAnything() {
        UpdateWorkTemplateDto dto = new UpdateWorkTemplateDto();

        when(workTemplateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(workTemplateRepository.save(any(WorkTemplate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WorkTemplate result = workTemplateService.update(1L, dto);

        assertEquals("Template A", result.getTemplateTitle());
        assertEquals("Template A description", result.getTemplateDescription());
        assertEquals("Work A", result.getWorkTitle());
        assertEquals("Work A description", result.getWorkDescription());
        assertEquals(ProjectType.COURSEWORK, result.getType());
        assertEquals(2, result.getCountOfChapters());

        verify(workTemplateRepository).save(template);
    }

    @Test
    void delete_ShouldCallRepositoryDeleteById() {
        workTemplateService.delete(1L);

        verify(workTemplateRepository).deleteById(1L);
    }

}
