package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateProjectService {

    private final ProjectService projectService;
    private final HistoryRecorder historyRecorder;

    /**
     * 프로젝트를 생성하고 관련 히스토리 및 멤버 정보를 저장
     * @param request 프로젝트 생성 요청 정보
     * @return 생성된 프로젝트 응답 정보
     */
    public ProjectResponse createProject(CreateProjectRequest request) {

        Project savedProject = saveProjectAndHistory(request);
        saveClientMembersAndHistory(request.managerNames(), savedProject.getProjectId());
        saveDevMembersAndHistory(request.developerNames(), savedProject.getProjectId());

        return ProjectResponse.from(savedProject);
    }

    /**
     * 프로젝트를 저장하고 프로젝트 히스토리를 함께 저장.
     * @param request 프로젝트 생성 요청 정보
     * @return 저장된 프로젝트 엔티티
     */
    private Project saveProjectAndHistory(CreateProjectRequest request) {

        Project savedProject = projectService.saveProject(Project.of(request));
        ProjectHistorySnapshot snapshot = ProjectHistorySnapshot.from(savedProject);

        historyRecorder.recordHistory(HistoryType.PROJECT, savedProject.getProjectId(), ActionType.CREATE, snapshot);

        return savedProject;
    }

    /**
     * 프로젝트 고객사 멤버를 저장하고 멤버 히스토리를 함께 저장.
     * @param memberIds 고객사 멤버 ID 리스트
     * @param projectId 프로젝트 ID
     */
    private void saveClientMembersAndHistory(List<Long> memberIds, Long projectId) {

        List<ProjectClientMember> clientMembers = memberIds.stream()
                .map(client -> ProjectClientMember.of(client, projectId))
                .toList();

        List<ProjectClientMember> savedProjectClientMember = projectService.saveProjectClientMember(clientMembers);

        savedProjectClientMember.forEach(member ->
                historyRecorder.recordHistory(
                        HistoryType.PROJECT_CLIENT_MEMBER,
                        member.getProjectClientMemberId(),
                        ActionType.CREATE,
                        member
                )
        );
    }

    /**
     * 프로젝트 개발사 멤버를 저장하고 멤버 히스토리를 함께 저장.
     * @param developerIds 개발사 멤버 ID 리스트
     * @param projectId 프로젝트 ID
     */
    private void saveDevMembersAndHistory(List<Long> developerIds, Long projectId) {

        List<ProjectDevMember> devMembers = developerIds.stream()
                .map(developer -> ProjectDevMember.of(developer, projectId))
                .toList();

        List<ProjectDevMember> savedProjectDevMember = projectService.saveProjectDevMember(devMembers);

        savedProjectDevMember.forEach(devMember ->
                historyRecorder.recordHistory(
                        HistoryType.PROJECT_DEV_MEMBER,
                        devMember.getProjectMemberId(),
                        ActionType.CREATE,
                        devMember
                )
        );
    }
}
