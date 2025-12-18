package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.SecurityUtil;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.dto.NodeSnapshot;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.event.ProjectNodeCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CreateProjectNodeService {

    private final ProjectNodeService projectNodeService;
    private final ProjectNodeValidator projectNodeValidator;
    private final HistoryRecorder historyRecorder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 프로젝트 노드를 생성하고 관련 히스토리를 저장
     *
     * @param projectId 프로젝트 ID
     * @param request   노드 생성 요청 정보
     * @return 생성된 프로젝트 노드 응답 정보
     */
    public CreateNodeResponse createNode(Long projectId, CreateNodeRequest request) {

        Long loginUser = SecurityUtil.getCurrentUserIdOrThrow();
        projectNodeValidator.validateLoginUserPermission(projectId, loginUser);

        Project project = projectNodeValidator.validateProjectAndDevMember(projectId, request.developerUserId());
        project.updateProjectStatus(Status.IN_PROGRESS);

        Integer nodeOrder = projectNodeService.findMaxNodeOrderByProjectId(projectId) + 1;
        ProjectNode savedProjectNode = saveProjectNodeAndHistory(projectId, request, nodeOrder);
        eventPublisher.publishEvent(new ProjectNodeCreatedEvent(projectId, savedProjectNode));

        return CreateNodeResponse.from(savedProjectNode);
    }

    /**
     * 프로젝트 노드를 저장하고 노드 히스토리를 함께 저장
     *
     * @param projectId 프로젝트 ID
     * @param request   노드 생성 요청 정보
     * @param nodeOrder 노드 순서
     * @return 생성된 프로젝트 노드 응답 정보
     */
    private ProjectNode saveProjectNodeAndHistory(Long projectId, CreateNodeRequest request, Integer nodeOrder) {

        ProjectNode savedProjectNode = projectNodeService.saveProjectNode(ProjectNode.of(projectId, request, nodeOrder));
        NodeSnapshot snapshot = NodeSnapshot.from(savedProjectNode);

        historyRecorder.recordHistory(HistoryType.PROJECT_NODE, savedProjectNode.getProjectNodeId(), ActionType.CREATE,
                snapshot);

        return savedProjectNode;
    }
}
