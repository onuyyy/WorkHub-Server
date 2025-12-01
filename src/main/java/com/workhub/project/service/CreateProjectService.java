package com.workhub.project.service;

import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.entity.*;
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

    /**
     * 프로젝트를 생성하고 관련 히스토리 및 멤버 정보를 저장
     * @param request 프로젝트 생성 요청 정보
     * @param userIp 요청한 사용자의 IP 주소
     * @param userAgent 요청한 사용자의 User Agent 정보
     * @return 생성된 프로젝트 응답 정보
     */
    public ProjectResponse createProject(CreateProjectRequest request, Long loginUser, String userIp, String userAgent) {

        Project savedProject = saveProjectAndHistory(request, loginUser, userIp, userAgent);
        saveClientMembersAndHistory(request.managerNames(), savedProject.getProjectId(), loginUser, userIp, userAgent);
        saveDevMembersAndHistory(request.developerNames(), savedProject.getProjectId(), loginUser, userIp, userAgent);

        return ProjectResponse.from(savedProject);
    }

    /**
     * 프로젝트를 저장하고 프로젝트 히스토리를 함께 저장.
     * @param request 프로젝트 생성 요청 정보
     * @param loginUser 로그인한 사용자 ID
     * @param userIp 요청한 사용자의 IP 주소
     * @param userAgent 요청한 사용자의 User Agent 정보
     * @return 저장된 프로젝트 엔티티
     */
    private Project saveProjectAndHistory(CreateProjectRequest request, Long loginUser, String userIp, String userAgent) {

        Project savedProject = projectService.saveProject(Project.of(request));
        ProjectHistory projectHistory = ProjectHistory.of(savedProject, loginUser, userIp, userAgent);
        projectService.saveProjectHistory(projectHistory);

        return savedProject;
    }

    /**
     * 프로젝트 고객사 멤버를 저장하고 멤버 히스토리를 함께 저장.
     * @param memberIds 고객사 멤버 ID 리스트
     * @param projectId 프로젝트 ID
     * @param loginUser 로그인한 사용자 ID
     * @param userIp 요청한 사용자의 IP 주소
     * @param userAgent 요청한 사용자의 User Agent 정보
     */
    private void saveClientMembersAndHistory(List<Long> memberIds, Long projectId, Long loginUser, String userIp, String userAgent) {

        List<ProjectClientMember> clientMembers = memberIds.stream()
                .map(client -> ProjectClientMember.of(client, projectId))
                .toList();

        List<ProjectClientMember> savedProjectClientMember = projectService.saveProjectClientMember(clientMembers);

        List<ProjectClientMemberHistory> clientMemberHistories = savedProjectClientMember.stream()
                .map(ProjectClientMember::getProjectClientMemberId)
                .map(memberId -> ProjectClientMemberHistory.of(memberId, loginUser, userIp, userAgent))
                .toList();

        projectService.saveProjectClientMemberHistory(clientMemberHistories);
    }

    /**
     * 프로젝트 개발사 멤버를 저장하고 멤버 히스토리를 함께 저장.
     * @param developerIds 개발사 멤버 ID 리스트
     * @param projectId 프로젝트 ID
     * @param loginUser 로그인한 사용자 ID
     * @param userIp 요청한 사용자의 IP 주소
     * @param userAgent 요청한 사용자의 User Agent 정보
     */
    private void saveDevMembersAndHistory(List<Long> developerIds, Long projectId, Long loginUser, String userIp, String userAgent) {

        List<ProjectDevMember> devMembers = developerIds.stream()
                .map(developer -> ProjectDevMember.of(developer, projectId))
                .toList();

        List<ProjectDevMember> savedProjectDevMember = projectService.saveProjectDevMember(devMembers);

        List<ProjectDevMemberHistory> devMemberHistories = savedProjectDevMember.stream()
                .map(ProjectDevMember::getProjectMemberId)
                .map(devId -> ProjectDevMemberHistory.of(devId, loginUser, userIp, userAgent))
                .toList();

        projectService.saveProjectDevMemberHistory(devMemberHistories);
    }
}
