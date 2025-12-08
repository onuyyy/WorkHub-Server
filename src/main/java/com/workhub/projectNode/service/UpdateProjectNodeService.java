package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.StatusValidator;
import com.workhub.projectNode.dto.NodeSnapshot;
import com.workhub.projectNode.dto.UpdateNodOrderRequest;
import com.workhub.projectNode.dto.UpdateNodeStatusRequest;
import com.workhub.projectNode.entity.ProjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UpdateProjectNodeService {

    private final ProjectNodeService projectNodeService;
    private final HistoryRecorder historyRecorder;

    /**
     * 프로젝트 노드 상태를 업데이트하고 변경 이력을 저장.
     * @param nodeId 업데이트할 프로젝트 노드 ID
     * @param request 변경할 상태 정보
     */
    public void updateNodeStatus(Long nodeId, UpdateNodeStatusRequest request) {

        ProjectNode original = projectNodeService.findById(nodeId);
        NodeSnapshot snapshot = NodeSnapshot.from(original);

        StatusValidator.validateStatusChange(original.getNodeStatus(), request.nodeStatus());
        original.updateNodeStatus(request.nodeStatus());

        historyRecorder.recordHistory(HistoryType.PROJECT_NODE, nodeId, ActionType.UPDATE, snapshot);

    }

    /**
     * 프로젝트 노드의 순서를 일괄 업데이트
     * @param projectId 프로젝트 ID
     * @param request 노드 순서 변경 요청 리스트
     */
    public void updateNodeOrder(Long projectId, List<UpdateNodOrderRequest> request) {

        List<ProjectNode> projectNodes = projectNodeService.findByProjectIdByNodeOrder(projectId);
        Map<Long, ProjectNode> nodeMap = projectNodes.stream()
                .collect(Collectors.toMap(ProjectNode::getProjectNodeId, Function.identity()));

        request.forEach(req -> {

            ProjectNode node = nodeMap.get(req.projectNodeId());

            if (node == null) {
                throw new BusinessException(ErrorCode.PROJECT_NODE_NOT_FOUND);
            }

            NodeSnapshot snapshot = NodeSnapshot.from(node);
            Integer beforeOrder = node.getNodeOrder();

            if (!beforeOrder.equals(req.nodeOrder())) {

                node.updateNodeOrder(req.nodeOrder());
                historyRecorder.recordHistory(HistoryType.PROJECT_NODE, req.projectNodeId(),
                        ActionType.UPDATE, snapshot
                );
            }
        });

    }
}
