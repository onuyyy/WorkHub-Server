package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.entity.ProjectNodeHistory;
import com.workhub.projectNode.repository.ProjectNodeHistoryRepository;
import com.workhub.projectNode.repository.ProjectNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectNodeService {

    private final ProjectNodeRepository projectNodeRepository;
    private final ProjectNodeHistoryRepository projectNodeHistoryRepository;

    public ProjectNode saveProjectNode(ProjectNode projectNode){
        return projectNodeRepository.save(projectNode);
    }

    public List<ProjectNode> findByProjectIdByNodeOrder(Long projectId) {
        return projectNodeRepository.findByProjectIdAndDeletedAtIsNullOrderByNodeOrderAsc(projectId);
    }

    public ProjectNode findById(Long projectNodeId){
        return projectNodeRepository.findById(projectNodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));
    }

    public Long getNodeOriginalCreator(Long projectNodeId){

        return projectNodeHistoryRepository
                .findFirstByTargetIdAndActionTypeOrderByChangeLogIdAsc(projectNodeId, ActionType.CREATE)
                .map(ProjectNodeHistory::getCreatedBy)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND));

    }

    /**
     * 프로젝트 노드 상태 변경 이력을 최초 저장.
     * @param nodeId 프로젝트 노드 ID
     * @param beforeStatus 변경 전 상태
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId 요청자 사용자 ID
     */
    public void createNodeHistory(Long nodeId, String beforeStatus,
                                     String userIp, String userAgent, Long userId) {

        projectNodeHistoryRepository.save(ProjectNodeHistory.of(nodeId,
                ActionType.CREATE, beforeStatus, userId,
                userId, userIp, userAgent));
    }

    /**
     * 프로젝트 상태 변경 이력을 저장.
     * @param nodeId 프로젝트 노드 ID
     * @param actionType 변경 액션
     * @param beforeStatus 변경 전 상태
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId 요청자 사용자 ID
     */
    public void updateNodeHistory(Long nodeId, ActionType actionType, String beforeStatus,
                                     String userIp, String userAgent, Long userId) {

        Long originalCreator = getNodeOriginalCreator(nodeId);

        projectNodeHistoryRepository.save(ProjectNodeHistory.of(nodeId,
                actionType, beforeStatus, originalCreator,
                userId, userIp, userAgent));
    }
}
