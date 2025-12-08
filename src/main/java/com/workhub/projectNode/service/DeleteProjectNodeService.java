package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.projectNode.dto.NodeSnapshot;
import com.workhub.projectNode.entity.ProjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class DeleteProjectNodeService {

    private final ProjectNodeService projectNodeService;
    private final HistoryRecorder historyRecorder;

    public void deleteProjectNode(Long projectId,Long nodeId) {

        ProjectNode original = projectNodeService.findByIdAndProjectId(nodeId, projectId);
        NodeSnapshot snapshot = NodeSnapshot.from(original);

        original.markDeleted();
        historyRecorder.recordHistory(HistoryType.PROJECT_NODE, nodeId, ActionType.DELETE, snapshot);
    }
}
