package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.dto.NodeSnapshot;
import com.workhub.projectNode.entity.ProjectNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CreateProjectNodeService {

    private final ProjectNodeService projectNodeService;
    private final HistoryRecorder historyRecorder;

    /**
     * 프로젝트 노드를 생성하고 관련 히스토리를 저장
     *
     * @param projectId 프로젝트 ID
     * @param request   노드 생성 요청 정보
     * @return 생성된 프로젝트 노드 응답 정보
     */
    public CreateNodeResponse createNode(Long projectId, CreateNodeRequest request) {

        List<ProjectNode> projectNodeList = projectNodeService.findByProjectIdByNodeOrder(projectId);

        Integer nodeOrder = calculateNodeOrder(projectNodeList);
        ProjectNode savedProjectNode = saveProjectNodeAndHistory(projectId, request, nodeOrder);

        return CreateNodeResponse.from(savedProjectNode);
    }

    /**
     * 새로운 노드의 순서를 계산
     * 프로젝트 노드 리스트가 비어있으면 1, 그렇지 않으면 마지막 노드 순서 + 1
     *
     * @param projectNodeList 프로젝트의 노드 리스트
     * @return 계산된 노드 순서
     */
    private Integer calculateNodeOrder(List<ProjectNode> projectNodeList) {
        if (projectNodeList.isEmpty()) {
            return 1;
        }
        return projectNodeList.getLast().getNodeOrder() + 1;
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
