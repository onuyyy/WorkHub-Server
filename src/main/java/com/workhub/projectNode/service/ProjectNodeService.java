package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
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

    /**
     * 프로젝트 노드 상태 변경 이력을 최초 저장.
     * @param nodeId 프로젝트 노드 ID
     * @param beforeStatus 변경 전 상태
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId 요청자 사용자 ID
     */
    public void createProjectHistory(Long nodeId, String beforeStatus,
                                     String userIp, String userAgent, Long userId) {

        projectNodeHistoryRepository.save(ProjectNodeHistory.of(nodeId,
                ActionType.CREATE, beforeStatus, userId,
                userId, userIp, userAgent));
    }

}
