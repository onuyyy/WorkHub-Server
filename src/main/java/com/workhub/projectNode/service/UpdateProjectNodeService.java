package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.StatusValidator;
import com.workhub.projectNode.dto.UpdateNodeStatusRequest;
import com.workhub.projectNode.entity.ProjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        String beforeStatus = original.getNodeStatus().toString();

        StatusValidator.validateStatusChange(original.getNodeStatus(), request.nodeStatus());
        original.updateNodeStatus(request.nodeStatus());

        historyRecorder.recordHistory(HistoryType.PROJECT_NODE, nodeId, ActionType.UPDATE, beforeStatus);

    }
}
