package com.workhub.project.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.*;
import com.workhub.project.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ClientMemberRepository clientMemberRepository;

    @Mock
    private DevMemberRepository devMemberRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project mockProject;
    private ProjectClientMember mockClientMember;
    private ProjectDevMember mockDevMember;

    @BeforeEach
    void init() {
        mockProject = Project.builder()
                .projectId(1L)
                .projectTitle("테스트 프로젝트")
                .projectDescription("테스트 설명")
                .status(Status.IN_PROGRESS)
                .contractStartDate(LocalDate.now())
                .contractEndDate(LocalDate.now().plusMonths(6))
                .clientCompanyId(100L)
                .build();

        mockClientMember = ProjectClientMember.builder()
                .projectClientMemberId(1L)
                .userId(1L)
                .projectId(1L)
                .role(Role.READ)
                .assignedAt(LocalDate.now())
                .build();

        mockDevMember = ProjectDevMember.builder()
                .projectMemberId(1L)
                .userId(2L)
                .projectId(1L)
                .devPart(DevPart.BE)
                .assignedAt(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("프로젝트를 저장하면 저장된 프로젝트를 반환한다.")
    void givenProject_whenSaveProject_thenReturnSavedProject() {
        when(projectRepository.save(any(Project.class))).thenReturn(mockProject);

        Project result = projectService.saveProject(mockProject);

        assertThat(result).isNotNull();
        assertThat(result.getProjectId()).isEqualTo(1L);
        assertThat(result.getProjectTitle()).isEqualTo("테스트 프로젝트");
        verify(projectRepository).save(mockProject);
    }

    @Test
    @DisplayName("존재하는 프로젝트 ID로 조회하면 프로젝트를 반환한다.")
    void givenValidProjectId_whenFindProjectById_thenReturnProject() {
        Long projectId = 1L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        Project result = projectService.findProjectById(projectId);

        assertThat(result).isNotNull();
        assertThat(result.getProjectId()).isEqualTo(projectId);
        assertThat(result.getProjectTitle()).isEqualTo("테스트 프로젝트");
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 ID로 조회하면 예외를 발생시킨다.")
    void givenInvalidProjectId_whenFindProjectById_thenThrowException() {
        Long projectId = 999L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findProjectById(projectId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("클라이언트 멤버 리스트를 저장하면 저장된 리스트를 반환한다.")
    void givenClientMembers_whenSaveProjectClientMember_thenReturnSavedList() {
        List<ProjectClientMember> clientMembers = Arrays.asList(mockClientMember);
        when(clientMemberRepository.saveAll(anyList())).thenReturn(clientMembers);

        List<ProjectClientMember> result = projectService.saveProjectClientMember(clientMembers);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectClientMemberId()).isEqualTo(1L);
        verify(clientMemberRepository).saveAll(clientMembers);
    }

    @Test
    @DisplayName("개발 멤버 리스트를 저장하면 저장된 리스트를 반환한다.")
    void givenDevMembers_whenSaveProjectDevMember_thenReturnSavedList() {
        List<ProjectDevMember> devMembers = Arrays.asList(mockDevMember);
        when(devMemberRepository.saveAll(anyList())).thenReturn(devMembers);

        List<ProjectDevMember> result = projectService.saveProjectDevMember(devMembers);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProjectMemberId()).isEqualTo(1L);
        verify(devMemberRepository).saveAll(devMembers);
    }

    @Test
    @DisplayName("완료된 프로젝트를 검증하면 프로젝트를 반환한다.")
    void givenCompletedProject_whenValidateCompletedProject_thenReturnProject() {
        Long projectId = 1L;
        Project completedProject = Project.builder()
                .projectId(projectId)
                .projectTitle("완료된 프로젝트")
                .status(Status.COMPLETED)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(completedProject));

        Project result = projectService.validateCompletedProject(projectId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.COMPLETED);
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("완료되지 않은 프로젝트를 검증하면 예외를 발생시킨다.")
    void givenNotCompletedProject_whenValidateCompletedProject_thenThrowException() {
        Long projectId = 1L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        assertThatThrownBy(() -> projectService.validateCompletedProject(projectId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST);

        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("진행 중인 프로젝트를 검증하면 프로젝트를 반환한다.")
    void givenInProgressProject_whenValidateProject_thenReturnProject() {
        Long projectId = 1L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(mockProject));

        Project result = projectService.validateProject(projectId);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.IN_PROGRESS);
        verify(projectRepository).findById(projectId);
    }

    @Test
    @DisplayName("진행 중이 아닌 프로젝트를 검증하면 예외를 발생시킨다.")
    void givenNotInProgressProject_whenValidateProject_thenThrowException() {
        Long projectId = 1L;
        Project completedProject = Project.builder()
                .projectId(projectId)
                .projectTitle("완료된 프로젝트")
                .status(Status.COMPLETED)
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(completedProject));

        assertThatThrownBy(() -> projectService.validateProject(projectId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);

        verify(projectRepository).findById(projectId);
    }
}