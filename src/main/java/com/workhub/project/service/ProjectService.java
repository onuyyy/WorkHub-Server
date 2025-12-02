package com.workhub.project.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.*;
import com.workhub.project.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectHistoryRepository projectHistoryRepository;
    private final ClientMemberRepository clientMemberRepository;
    private final DevMemberRepository devMemberRepository;
    private final ClientMemberHistoryRepository  clientMemberHistoryRepository;
    private final DevMemberHistoryRepository devMemberHistoryRepository;

    public Project saveProject(Project project){
        return projectRepository.save(project);
    }

    public Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));
    }

    public List<ProjectClientMember> saveProjectClientMember(List<ProjectClientMember> projectClientMembers){
        return clientMemberRepository.saveAll(projectClientMembers);
    }

    public List<ProjectDevMember> saveProjectDevMember(List<ProjectDevMember> projectDevMembers){
        return devMemberRepository.saveAll(projectDevMembers);
    }

    public void saveProjectHistory(ProjectHistory projectHistory){
        projectHistoryRepository.save(projectHistory);
    }

    public void saveProjectClientMemberHistory(List<ProjectClientMemberHistory> clientMemberHistories){
        clientMemberHistoryRepository.saveAll(clientMemberHistories);
    }

    public void saveProjectDevMemberHistory(List<ProjectDevMemberHistory> projectDevMemberHistories){
        devMemberHistoryRepository.saveAll(projectDevMemberHistories);
    }

    public Long getProjectOriginalCreator(Long projectId){

        return projectHistoryRepository
                .findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(projectId, ActionType.CREATE)
                .map(ProjectHistory::getCreatedBy)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_HISTORY_NOT_FOUND));

    }

    /**
     * 프로젝트 상태 변경 이력을 저장.
     * @param projectId 프로젝트 ID
     * @param actionType 변경 액션
     * @param beforeStatus 변경 전 상태
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId 요청자 사용자 ID
     */
    public void updateProjectHistory(Long projectId, ActionType actionType, String beforeStatus,
                                     String userIp, String userAgent, Long userId) {

        Long originalCreator = getProjectOriginalCreator(projectId);

        projectHistoryRepository.save(ProjectHistory.of(projectId,
                actionType, beforeStatus, originalCreator,
                userId, userIp, userAgent));
    }

    public Project validateCompletedProject(Long projectId) {
        Project project = findProjectById(projectId);
        if (!Status.COMPLETED.equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_CS_POST);
        }
        return project;
    }
    public Project validateProject(Long projectId) {
        Project project = findProjectById(projectId);
        if (!Status.IN_PROGRESS.equals(project.getStatus())) {
            throw new BusinessException(ErrorCode.INVALID_PROJECT_STATUS_FOR_POST);
        }
        return project;
    }
}
