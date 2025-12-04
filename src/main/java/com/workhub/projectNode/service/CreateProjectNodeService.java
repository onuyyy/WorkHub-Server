package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.projectNode.entity.ProjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        
        adjustNodeOrdersIfNecessary(projectNodeList, request.nodeOrder());
        ProjectNode savedProjectNode = saveProjectNodeAndHistory(projectId, request);

        return CreateNodeResponse.from(savedProjectNode);
    }

    /**
     * 기존 노드들의 순서를 조정
     * 요청된 node_order와 같은 순서의 노드가 존재하는 경우,
     * 해당 노드부터 그 이후 노드까지의 node_order를 1씩 증가
     * @param projectNodeList 프로젝트의 노드 리스트
     * @param requestNodeOrder 요청된 노드 순서
     */
    private void adjustNodeOrdersIfNecessary(List<ProjectNode> projectNodeList, Integer requestNodeOrder) {

        boolean hasSameOrder = projectNodeList.stream()
                .anyMatch(node -> node.getNodeOrder().equals(requestNodeOrder));

        if(hasSameOrder) {
            projectNodeList.stream()
                    .filter(node -> node.getNodeOrder() >= requestNodeOrder)
                    .forEach(ProjectNode::incrementNodeOrder);
        }
    }

    /**
     * 프로젝트 노드를 저장하고 노드 히스토리를 함께 저장
     *
     * @param projectId 프로젝트 ID
     * @param request   노드 생성 요청 정보
     * @return 생성된 프로젝트 노드 응답 정보
     */
    private ProjectNode saveProjectNodeAndHistory(Long projectId, CreateNodeRequest request) {

        ProjectNode savedProjectNode = projectNodeService.saveProjectNode(ProjectNode.of(projectId, request));

        historyRecorder.recordHistory(HistoryType.PROJECT_NODE, savedProjectNode.getProjectNodeId(), ActionType.CREATE,
                savedProjectNode.getDescription());

        return savedProjectNode;
    }
}
