package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
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

    /**
     * 프로젝트 노드 상태를 업데이트하고 변경 이력을 저장.
     * @param nodeId 업데이트할 프로젝트 노드 ID
     * @param request 변경할 상태 정보
     * @param userIp 요청자 IP 주소
     * @param userAgent 요청자 User-Agent
     * @param userId 요청자 사용자 ID
     */
    public void updateNodeStatus(Long nodeId, UpdateNodeStatusRequest request,
                                 String userIp, String userAgent, Long userId) {

        ProjectNode original = projectNodeService.findById(nodeId);
        String beforeStatus = original.getNodeStatus().toString();

        StatusValidator.validateStatusChange(original.getNodeStatus(), request.nodeStatus());

        original.updateNodeStatus(request.nodeStatus());
        projectNodeService.updateNodeHistory(nodeId, ActionType.UPDATE, beforeStatus,
                userIp, userAgent, userId);

    }
}
