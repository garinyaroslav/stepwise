package com.github.stepwise.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.stepwise.entity.AcademicWork;
import com.github.stepwise.entity.DefenseRegistration;
import com.github.stepwise.entity.DefenseSchedule;
import com.github.stepwise.entity.Profile;
import com.github.stepwise.entity.Project;
import com.github.stepwise.entity.ProjectStatus;
import com.github.stepwise.entity.User;
import com.github.stepwise.exception.NotFoundException;
import com.github.stepwise.repository.AcademicWorkRepository;
import com.github.stepwise.repository.DefenceRegistrationRepository;
import com.github.stepwise.repository.DefenseScheduleRepository;
import com.github.stepwise.repository.DefenseScheduleRepository.ScheduleRegistrationCount;
import com.github.stepwise.repository.ProjectRepository;
import com.github.stepwise.web.dto.DefenseDto;
import com.github.stepwise.web.dto.DefenseDto.CreateScheduleDto;
import com.github.stepwise.web.dto.DefenseDto.RegistrationResponseDto;
import com.github.stepwise.web.dto.DefenseDto.ScheduleResponseDto;

@ExtendWith(MockitoExtension.class)
class DefenseServiceTest {

    @Mock
    private DefenseScheduleRepository scheduleRepository;

    @Mock
    private DefenceRegistrationRepository registrationRepository;

    @Mock
    private AcademicWorkRepository academicWorkRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private DefenseService defenseService;

    private AcademicWork academicWork;
    private DefenseSchedule schedule;
    private User student;
    private Project project;

    @BeforeEach
    void setUp() {
        academicWork = AcademicWork.builder()
                .id(1L)
                .build();

        schedule = DefenseSchedule.builder()
                .id(10L)
                .academicWork(academicWork)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .maxStudents(5)
                .build();

        student = User.builder()
                .id(100L)
                .username("student1")
                .build();

        project = Project.builder()
                .id(1000L)
                .student(student)
                .status(ProjectStatus.APPROVED_FOR_DEFENSE)
                .build();
    }

    @Test
    void createSchedule_WhenWorkExists_ShouldCreateAndReturnSchedule() {
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setAcademicWorkId(1L);
        dto.setStartTime(LocalDateTime.now().plusDays(1));
        dto.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        dto.setMaxStudents(5);
        dto.setComment("comment");

        when(academicWorkRepository.findById(1L)).thenReturn(Optional.of(academicWork));
        when(scheduleRepository.save(any(DefenseSchedule.class))).thenAnswer(invocation -> {
            DefenseSchedule saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        ScheduleResponseDto result = defenseService.createSchedule(dto);

        assertNotNull(result);
        verify(academicWorkRepository).findById(1L);
        verify(scheduleRepository).save(any(DefenseSchedule.class));
    }

    @Test
    void createSchedule_WhenWorkNotFound_ShouldThrowException() {
        CreateScheduleDto dto = new CreateScheduleDto();
        dto.setAcademicWorkId(999L);

        when(academicWorkRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> defenseService.createSchedule(dto));

        assertEquals("Academic work not found: 999", exception.getMessage());
        verify(scheduleRepository, never()).save(any(DefenseSchedule.class));
    }

    @Test
    void deleteSchedule_WhenExists_ShouldDeleteAndReturnResponse() {
        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(scheduleRepository.countRegistrations(10L)).thenReturn(3);

        ScheduleResponseDto result = defenseService.deleteSchedule(10L);

        assertNotNull(result);
        verify(scheduleRepository).delete(schedule);
    }

    @Test
    void deleteSchedule_WhenNotFound_ShouldThrowException() {
        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> defenseService.deleteSchedule(999L));

        assertEquals("Schedule not found: 999", exception.getMessage());
        verify(scheduleRepository, never()).delete(any(DefenseSchedule.class));
    }

    @Test
    void getSchedulesByWork_WhenNoSchedules_ShouldReturnEmptyListWithoutCountQuery() {
        when(scheduleRepository.findByAcademicWorkId(1L)).thenReturn(List.of());

        List<ScheduleResponseDto> result = defenseService.getSchedulesByWork(1L);

        assertTrue(result.isEmpty());
        verify(scheduleRepository, never()).countRegistrationsByScheduleIds(anyList());
    }

    @Test
    void getSchedulesByWork_WithSchedules_ShouldReturnCountsInSingleBatchQuery() {
        DefenseSchedule schedule2 = DefenseSchedule.builder().id(11L).academicWork(academicWork).build();

        when(scheduleRepository.findByAcademicWorkId(1L)).thenReturn(List.of(schedule, schedule2));

        ScheduleRegistrationCount count1 = mock(ScheduleRegistrationCount.class);
        when(count1.getScheduleId()).thenReturn(10L);
        when(count1.getCount()).thenReturn(3L);

        ScheduleRegistrationCount count2 = mock(ScheduleRegistrationCount.class);
        when(count2.getScheduleId()).thenReturn(11L);
        when(count2.getCount()).thenReturn(0L);

        when(scheduleRepository.countRegistrationsByScheduleIds(List.of(10L, 11L)))
                .thenReturn(List.of(count1, count2));

        List<ScheduleResponseDto> result = defenseService.getSchedulesByWork(1L);

        assertEquals(2, result.size());
        verify(scheduleRepository, times(1)).countRegistrationsByScheduleIds(anyList());
        verify(scheduleRepository, never()).countRegistrations(anyLong());
    }

    @Test
    void getSchedulesByWork_WhenScheduleMissingFromCountResult_ShouldDefaultToZero() {
        when(scheduleRepository.findByAcademicWorkId(1L)).thenReturn(List.of(schedule));
        when(scheduleRepository.countRegistrationsByScheduleIds(List.of(10L))).thenReturn(List.of());

        List<ScheduleResponseDto> result = defenseService.getSchedulesByWork(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getRegistrationsForSchedule_WhenScheduleNotFound_ShouldThrowException() {
        when(scheduleRepository.existsById(999L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> defenseService.getRegistrationsForSchedule(999L));

        assertEquals("Schedule not found: 999", exception.getMessage());
        verify(registrationRepository, never()).findByScheduleIdWithStudentDetails(anyLong());
    }

    @Test
    void getRegistrationsForSchedule_ShouldReturnSortedDetailsWithProfile() {
        Profile profile = Profile.builder().firstName("John").lastName("Doe").build();
        User studentWithProfile = User.builder().id(100L).username("student1").profile(profile).build();
        Project projectWithProfile = Project.builder().id(1000L).student(studentWithProfile).build();

        DefenseRegistration reg1 = DefenseRegistration.builder()
                .id(1L).project(projectWithProfile).orderNumber(2).registeredAt(LocalDateTime.now()).build();
        DefenseRegistration reg2 = DefenseRegistration.builder()
                .id(2L).project(projectWithProfile).orderNumber(1).registeredAt(LocalDateTime.now()).build();

        when(scheduleRepository.existsById(10L)).thenReturn(true);
        when(registrationRepository.findByScheduleIdWithStudentDetails(10L)).thenReturn(List.of(reg1, reg2));

        List<DefenseDto.RegistrationDetailsDto> result = defenseService.getRegistrationsForSchedule(10L);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderNumber());
        assertEquals(2, result.get(1).getOrderNumber());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
        assertEquals("student1", result.get(0).getUsername());
    }

    @Test
    void getRegistrationsForSchedule_WhenStudentHasNoProfile_ShouldReturnNullNames() {
        User studentWithoutProfile = User.builder().id(100L).username("student1").profile(null).build();
        Project projectWithoutProfile = Project.builder().id(1000L).student(studentWithoutProfile).build();
        DefenseRegistration reg = DefenseRegistration.builder()
                .id(1L).project(projectWithoutProfile).orderNumber(1).registeredAt(LocalDateTime.now()).build();

        when(scheduleRepository.existsById(10L)).thenReturn(true);
        when(registrationRepository.findByScheduleIdWithStudentDetails(10L)).thenReturn(List.of(reg));

        List<DefenseDto.RegistrationDetailsDto> result = defenseService.getRegistrationsForSchedule(10L);

        assertNull(result.get(0).getFirstName());
        assertNull(result.get(0).getLastName());
        assertEquals("student1", result.get(0).getUsername());
    }

    @Test
    void getRegistrationsForSchedule_WithNullOrderNumbers_ShouldSortNullsLast() {
        DefenseRegistration regWithOrder = DefenseRegistration.builder()
                .id(1L).project(project).orderNumber(1).registeredAt(LocalDateTime.now()).build();
        DefenseRegistration regWithoutOrder = DefenseRegistration.builder()
                .id(2L).project(project).orderNumber(null).registeredAt(LocalDateTime.now()).build();

        when(scheduleRepository.existsById(10L)).thenReturn(true);
        when(registrationRepository.findByScheduleIdWithStudentDetails(10L))
                .thenReturn(List.of(regWithoutOrder, regWithOrder));

        List<DefenseDto.RegistrationDetailsDto> result = defenseService.getRegistrationsForSchedule(10L);

        assertEquals(1, result.get(0).getOrderNumber());
        assertNull(result.get(1).getOrderNumber());
    }

    @Test
    void register_WhenEligible_ShouldCreateRegistration() {
        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(projectRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.of(project));
        when(registrationRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.empty());
        when(scheduleRepository.countRegistrations(10L)).thenReturn(2);
        when(registrationRepository.save(any(DefenseRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationResponseDto result = defenseService.register(10L, 100L);

        assertNotNull(result);
        ArgumentCaptor<DefenseRegistration> captor = org.mockito.ArgumentCaptor.forClass(DefenseRegistration.class);
        verify(registrationRepository).save(captor.capture());
        assertEquals(3, captor.getValue().getOrderNumber());
    }

    @Test
    void register_WhenScheduleNotFound_ShouldThrowException() {
        when(scheduleRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> defenseService.register(999L, 100L));

        assertEquals("Schedule not found: 999", exception.getMessage());
        verify(registrationRepository, never()).save(any(DefenseRegistration.class));
    }

    @Test
    void register_WhenProjectNotFound_ShouldThrowException() {
        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(projectRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> defenseService.register(10L, 100L));

        assertEquals("Project not found for student 100 and work 1", exception.getMessage());
        verify(registrationRepository, never()).save(any(DefenseRegistration.class));
    }

    @Test
    void register_WhenProjectAlreadyDefended_ShouldThrowException() {
        project.setStatus(ProjectStatus.DEFENDED);

        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(projectRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.of(project));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> defenseService.register(10L, 100L));

        assertEquals("Project is already defended", exception.getMessage());
        verify(registrationRepository, never()).save(any(DefenseRegistration.class));
    }

    @Test
    void register_WhenProjectNotApprovedForDefense_ShouldThrowException() {
        project.setStatus(ProjectStatus.IN_PROGRESS);

        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(projectRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.of(project));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> defenseService.register(10L, 100L));

        assertEquals("Student is not approved for defense yet", exception.getMessage());
        verify(registrationRepository, never()).save(any(DefenseRegistration.class));
    }

    @Test
    void register_WhenScheduleIsFull_ShouldThrowException() {
        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(projectRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.of(project));
        when(registrationRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.empty());
        when(scheduleRepository.countRegistrations(10L)).thenReturn(5);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> defenseService.register(10L, 100L));

        assertEquals("This defense session is full", exception.getMessage());
        verify(registrationRepository, never()).save(any(DefenseRegistration.class));
    }

    @Test
    void register_WhenPreviousSessionAlreadyEnded_ShouldRemoveOldRegistrationAndCreateNew() {
        DefenseSchedule previousSchedule = DefenseSchedule.builder()
                .id(9L)
                .academicWork(academicWork)
                .startTime(LocalDateTime.now().minusDays(2))
                .endTime(LocalDateTime.now().minusDays(1))
                .build();
        DefenseRegistration existingRegistration = DefenseRegistration.builder()
                .id(5L)
                .defenseSchedule(previousSchedule)
                .project(project)
                .build();

        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(projectRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.of(project));
        when(registrationRepository.findByStudentIdAndAcademicWorkId(100L, 1L))
                .thenReturn(Optional.of(existingRegistration));
        when(scheduleRepository.countRegistrations(10L)).thenReturn(0);
        when(registrationRepository.save(any(DefenseRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationResponseDto result = defenseService.register(10L, 100L);

        assertNotNull(result);
        verify(registrationRepository).delete(existingRegistration);
        verify(registrationRepository).flush();
        verify(registrationRepository).save(any(DefenseRegistration.class));
    }

    @Test
    void register_WhenPreviousSessionStillOngoing_ShouldThrowException() {
        DefenseSchedule previousSchedule = DefenseSchedule.builder()
                .id(9L)
                .academicWork(academicWork)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusHours(1))
                .build();
        DefenseRegistration existingRegistration = DefenseRegistration.builder()
                .id(5L)
                .defenseSchedule(previousSchedule)
                .project(project)
                .build();

        when(scheduleRepository.findById(10L)).thenReturn(Optional.of(schedule));
        when(projectRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.of(project));
        when(registrationRepository.findByStudentIdAndAcademicWorkId(100L, 1L))
                .thenReturn(Optional.of(existingRegistration));

        assertThrows(IllegalStateException.class, () -> defenseService.register(10L, 100L));

        verify(registrationRepository, never()).delete(any(DefenseRegistration.class));
        verify(registrationRepository, never()).save(any(DefenseRegistration.class));
    }

    @Test
    void getMyRegistration_WhenExists_ShouldReturnRegistration() {
        DefenseRegistration registration = DefenseRegistration.builder()
                .id(1L).defenseSchedule(schedule).project(project).registeredAt(LocalDateTime.now()).build();

        when(registrationRepository.findByStudentIdAndAcademicWorkId(100L, 1L))
                .thenReturn(Optional.of(registration));

        RegistrationResponseDto result = defenseService.getMyRegistration(1L, 100L);

        assertNotNull(result);
    }

    @Test
    void getMyRegistration_WhenNotExists_ShouldThrowException() {
        when(registrationRepository.findByStudentIdAndAcademicWorkId(100L, 1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> defenseService.getMyRegistration(1L, 100L));

        assertEquals("No registration found for student 100 and work 1", exception.getMessage());
    }

}
