package com.workhub.project.service;

import com.workhub.project.entity.*;
import com.workhub.project.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    ProjectRepository projectRepository;

    @Mock
    ProjectHistoryRepository projectHistoryRepository;

    @Mock
    ClientMemberRepository clientMemberRepository;

    @Mock
    DevMemberRepository devMemberRepository;

    @Mock
    ClientMemberHistoryRepository clientMemberHistoryRepository;

    @Mock
    DevMemberHistoryRepository devMemberHistoryRepository;

    @InjectMocks
    ProjectService projectService;

    @Test
    @DisplayName("프로젝트를 저장하면 저장된 프로젝트가 반환된다")
    void saveProject_success_shouldReturnSavedProject() {
        // given
        Project project = Project.builder()
                .projectTitle("Test Project")
                .build();

        Project savedProject = Project.builder()
                .projectId(1L)
                .projectTitle("Test Project")
                .build();

        given(projectRepository.save(any(Project.class))).willReturn(savedProject);

        // when
        Project result = projectService.saveProject(project);

        // then
        assertThat(result.getProjectId()).isEqualTo(1L);
        assertThat(result.getProjectTitle()).isEqualTo("Test Project");
        verify(projectRepository).save(project);
    }

    @Test
    @DisplayName("프로젝트 히스토리를 저장하면 repository.save가 호출된다")
    void saveProjectHistory_success_shouldInvokeSave() {
        // given
        ProjectHistory projectHistory = ProjectHistory.builder()
                .targetId(1L)
                .build();

        // when
        projectService.saveProjectHistory(projectHistory);

        // then
        verify(projectHistoryRepository).save(projectHistory);
    }

    @Test
    @DisplayName("고객사 멤버 리스트를 저장하면 저장된 리스트가 반환된다")
    void saveProjectClientMember_success_shouldReturnSavedList() {
        // given
        List<ProjectClientMember> clientMembers = Arrays.asList(
                ProjectClientMember.builder().projectClientMemberId(1L).build(),
                ProjectClientMember.builder().projectClientMemberId(2L).build()
        );

        given(clientMemberRepository.saveAll(any(List.class))).willReturn(clientMembers);

        // when
        List<ProjectClientMember> result = projectService.saveProjectClientMember(clientMembers);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProjectClientMemberId()).isEqualTo(1L);
        verify(clientMemberRepository).saveAll(clientMembers);
    }

    @Test
    @DisplayName("개발사 멤버 리스트를 저장하면 저장된 리스트가 반환된다")
    void saveProjectDevMember_success_shouldReturnSavedList() {
        // given
        List<ProjectDevMember> devMembers = Arrays.asList(
                ProjectDevMember.builder().projectMemberId(1L).build(),
                ProjectDevMember.builder().projectMemberId(2L).build()
        );

        given(devMemberRepository.saveAll(any(List.class))).willReturn(devMembers);

        // when
        List<ProjectDevMember> result = projectService.saveProjectDevMember(devMembers);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProjectMemberId()).isEqualTo(1L);
        verify(devMemberRepository).saveAll(devMembers);
    }

    @Test
    @DisplayName("고객사 멤버 히스토리를 저장하면 repository.saveAll이 호출된다")
    void saveProjectClientMemberHistory_success_shouldInvokeSaveAll() {
        // given
        List<ProjectClientMemberHistory> histories = Arrays.asList(
                ProjectClientMemberHistory.builder().build(),
                ProjectClientMemberHistory.builder().build()
        );

        // when
        projectService.saveProjectClientMemberHistory(histories);

        // then
        verify(clientMemberHistoryRepository).saveAll(histories);
    }

    @Test
    @DisplayName("개발사 멤버 히스토리를 저장하면 repository.saveAll이 호출된다")
    void saveProjectDevMemberHistory_success_shouldInvokeSaveAll() {
        // given
        List<ProjectDevMemberHistory> histories = Arrays.asList(
                ProjectDevMemberHistory.builder().build(),
                ProjectDevMemberHistory.builder().build()
        );

        // when
        projectService.saveProjectDevMemberHistory(histories);

        // then
        verify(devMemberHistoryRepository).saveAll(histories);
    }
}