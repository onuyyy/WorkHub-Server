package com.workhub.projectNode.service;

import com.workhub.global.history.HistoryRecorder;
import com.workhub.projectNode.dto.NodeListResponse;
import com.workhub.projectNode.entity.ProjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReadProjectNodeService {

    private final ProjectNodeService projectNodeService;
    private final HistoryRecorder historyRecorder;

    /**
     * 프로젝트의 모든 노드 리스트를 조회
     * 노드는 순서(nodeOrder)를 기준으로 정렬되어 반환됩니다.
     *
     * @param projectId 프로젝트 ID
     * @return 프로젝트에 속한 노드 리스트 (nodeOrder 기준 정렬)
     */
    public List<NodeListResponse> getNodeListByProject(Long projectId) {

        List<ProjectNode> nodeList = projectNodeService.findByProjectIdByNodeOrder(projectId);

        return nodeList.stream()
                .map(NodeListResponse::from)
                .toList();
    }
}
