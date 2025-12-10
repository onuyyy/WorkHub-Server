package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.project.dto.request.CreateProjectRequest;
import com.workhub.project.dto.ProjectHistorySnapshot;
import com.workhub.project.dto.response.ProjectResponse;
import com.workhub.project.dto.request.UpdateStatusRequest;
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
public class UpdateProjectService {

    private final ProjectService projectService;
    private final HistoryRecorder historyRecorder;

    /**
     * 프로젝트 상태를 업데이트하고 변경 이력을 저장.
     * @param projectId 업데이트할 프로젝트 ID
     * @param statusRequest 변경할 상태 정보
     */
    public void updateProjectStatus(Long projectId,
                                    UpdateStatusRequest statusRequest) {

        Project original = projectService.findProjectById(projectId);
        ProjectHistorySnapshot snapshot = ProjectHistorySnapshot.from(original);
        //String beforeStatus = original.getStatus().toString();

        original.updateProjectStatus(statusRequest.status());
        historyRecorder.recordHistory(HistoryType.PROJECT, projectId, ActionType.UPDATE, snapshot);

    }

    /**
     * 프로젝트 정보를 업데이트하고 변경된 필드별로 이력을 저장.
     * 변경된 필드만 감지하여 각 필드마다 개별 히스토리 레코드를 생성.
     *
     * @param projectId 업데이트할 프로젝트 ID
     * @param request   업데이트할 프로젝트 정보
     * @return 변경된 엔티티 응답
     */
    public ProjectResponse updateProject(Long projectId, CreateProjectRequest request) {

        // 1. 프로젝트 업데이트
        Project updatedProject = updateProjectAndHistory(projectId, request);

        // 2. 기존 멤버 조회
        List<ProjectClientMember> existingClientMembers = projectService.getClientMemberByProjectId(projectId);
        List<ProjectDevMember> existingDevMembers = projectService.getDevMemberByProjectId(projectId);

        // 3. 변경사항 결정
        List<Long> toRemoveClientIds = determineClientMembersToRemove(existingClientMembers, request.managerIds());
        List<Long> toAddClientIds = determineClientMembersToAdd(existingClientMembers, request.managerIds());
        List<Long> toRemoveDevIds = determineDevMembersToRemove(existingDevMembers, request.developerIds());
        List<Long> toAddDevIds = determineDevMembersToAdd(existingDevMembers, request.developerIds());

        // 4. 멤버 업데이트
        removeClientMembersAndHistory(existingClientMembers, toRemoveClientIds, projectId);
        removeDevMembersAndHistory(existingDevMembers, toRemoveDevIds, projectId);
        saveClientMembersAndHistory(toAddClientIds, projectId);
        saveDevMembersAndHistory(toAddDevIds, projectId);

        return ProjectResponse.from(updatedProject);
    }

    /**
     * 프로젝트에 배정된 고객사 멤버를 저장하고, 히스토리를 함께 저장
     * @param clientIds 고객사 멤버 PK
     * @param projectId 프로젝트 PK
     */
    private void saveClientMembersAndHistory(List<Long> clientIds, Long projectId) {

        List<ProjectClientMember> savedClientMembers = projectService.saveClientMembers(clientIds, projectId);

        savedClientMembers.forEach(member ->
                historyRecorder.recordHistory(
                        HistoryType.PROJECT_CLIENT_MEMBER,
                        member.getProjectClientMemberId(),
                        ActionType.CREATE,
                        member
                )
        );
    }

    /**
     * 프로젝트에 배정된 개발자 멤버를 저장하고, 히스토리를 함께 저장
     * @param devIds 개발자 멤버 PK
     * @param projectId 프로젝트 PK
     */
    private void saveDevMembersAndHistory(List<Long> devIds, Long projectId) {

        List<ProjectDevMember> savedDevMembers = projectService.saveDevMembers(devIds, projectId);

        savedDevMembers.forEach(member ->
                historyRecorder.recordHistory(
                        HistoryType.PROJECT_DEV_MEMBER,
                        member.getProjectMemberId(),
                        ActionType.CREATE,
                        member
                )
        );
    }

    /**
     * 프로젝트를 업데이트하고 히스토리를 기록
     * @param projectId 프로젝트 ID
     * @param request 업데이트 요청
     * @return 업데이트된 프로젝트
     */
    private Project updateProjectAndHistory(Long projectId, CreateProjectRequest request) {
        Project original = projectService.findProjectById(projectId);
        ProjectHistorySnapshot snapshot = ProjectHistorySnapshot.from(original);

        original.update(request);
        historyRecorder.recordHistory(HistoryType.PROJECT, projectId, ActionType.UPDATE, snapshot);

        return original;
    }

    /**
     * 클라이언트 멤버를 삭제하고 히스토리를 기록
     * @param existingMembers 기존 클라이언트 멤버 리스트
     * @param toRemoveIds 삭제할 멤버 ID 리스트
     * @param projectId 프로젝트 ID
     */
    private void removeClientMembersAndHistory(
            List<ProjectClientMember> existingMembers,
            List<Long> toRemoveIds,
            Long projectId
    ) {
        toRemoveIds.forEach(userId -> {
            ProjectClientMember member = existingMembers.stream()
                    .filter(m -> m.getUserId().equals(userId))
                    .findFirst()
                    .orElseThrow();
            member.removeMember();
            historyRecorder.recordHistory(HistoryType.PROJECT_CLIENT_MEMBER, projectId, ActionType.DELETE, member);
        });
    }

    /**
     * 개발자 멤버를 삭제하고 히스토리를 기록
     * @param existingMembers 기존 개발자 멤버 리스트
     * @param toRemoveIds 삭제할 멤버 ID 리스트
     * @param projectId 프로젝트 ID
     */
    private void removeDevMembersAndHistory(
            List<ProjectDevMember> existingMembers,
            List<Long> toRemoveIds,
            Long projectId
    ) {
        toRemoveIds.forEach(userId -> {
            ProjectDevMember member = existingMembers.stream()
                    .filter(m -> m.getUserId().equals(userId))
                    .findFirst()
                    .orElseThrow();
            member.removeMember();
            historyRecorder.recordHistory(HistoryType.PROJECT_DEV_MEMBER, projectId, ActionType.DELETE, member);
        });
    }

    /**
     * 삭제할 클라이언트 멤버 ID 결정
     * @param existingMembers 기존 클라이언트 멤버
     * @param requestedIds 요청된 클라이언트 멤버 ID
     * @return 삭제할 멤버 ID 리스트
     */
    private List<Long> determineClientMembersToRemove(
            List<ProjectClientMember> existingMembers,
            List<Long> requestedIds
    ) {
        List<Long> existingIds = existingMembers.stream()
                .map(ProjectClientMember::getUserId)
                .toList();
        return existingIds.stream()
                .filter(id -> !requestedIds.contains(id))
                .toList();
    }

    /**
     * 추가할 클라이언트 멤버 ID 결정
     * @param existingMembers 기존 클라이언트 멤버
     * @param requestedIds 요청된 클라이언트 멤버 ID
     * @return 추가할 멤버 ID 리스트
     */
    private List<Long> determineClientMembersToAdd(
            List<ProjectClientMember> existingMembers,
            List<Long> requestedIds
    ) {
        List<Long> existingIds = existingMembers.stream()
                .map(ProjectClientMember::getUserId)
                .toList();
        return requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
    }

    /**
     * 삭제할 개발자 멤버 ID 결정
     * @param existingMembers 기존 개발자 멤버
     * @param requestedIds 요청된 개발자 멤버 ID
     * @return 삭제할 멤버 ID 리스트
     */
    private List<Long> determineDevMembersToRemove(
            List<ProjectDevMember> existingMembers,
            List<Long> requestedIds
    ) {
        List<Long> existingIds = existingMembers.stream()
                .map(ProjectDevMember::getUserId)
                .toList();
        return existingIds.stream()
                .filter(id -> !requestedIds.contains(id))
                .toList();
    }

    /**
     * 추가할 개발자 멤버 ID 결정
     * @param existingMembers 기존 개발자 멤버
     * @param requestedIds 요청된 개발자 멤버 ID
     * @return 추가할 멤버 ID 리스트
     */
    private List<Long> determineDevMembersToAdd(
            List<ProjectDevMember> existingMembers,
            List<Long> requestedIds
    ) {
        List<Long> existingIds = existingMembers.stream()
                .map(ProjectDevMember::getUserId)
                .toList();
        return requestedIds.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
    }

}
