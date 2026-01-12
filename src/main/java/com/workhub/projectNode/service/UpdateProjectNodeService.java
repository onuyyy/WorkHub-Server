package com.workhub.projectNode.service;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.SecurityUtil;
import com.workhub.global.util.StatusValidator;
import com.workhub.projectNode.dto.*;
import com.workhub.projectNode.entity.ConfirmStatus;
import com.workhub.projectNode.entity.NodeStatus;
import com.workhub.projectNode.entity.ProjectNode;
import com.workhub.projectNode.event.ProjectNodeApprovedEvent;
import com.workhub.projectNode.event.ProjectNodeRejectedEvent;
import com.workhub.projectNode.event.ProjectNodeReviewRequestedEvent;
import com.workhub.projectNode.event.ProjectNodeUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ProjectNodeValidator projectNodeValidator;
    private final HistoryRecorder historyRecorder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 프로젝트 노드 상태를 업데이트하고 변경 이력을 저장.
     * @param projectId 프로젝트 ID
     * @param nodeId 업데이트할 프로젝트 노드 ID
     * @param request 변경할 상태 정보
     */
    public void updateNodeStatus(Long projectId, Long nodeId, UpdateNodeStatusRequest request) {

        ProjectNode original = getOriginalAndSaveHistory(projectId, nodeId);
        updateNodeStatus(original, request.nodeStatus());

        eventPublisher.publishEvent(new ProjectNodeUpdatedEvent(projectId, nodeId, original.getTitle(), "상태"));
    }

    /**
     * 프로젝트 노드의 순서를 일괄 업데이트
     * @param projectId 프로젝트 ID
     * @param request 노드 순서 변경 요청 리스트
     */
    public void updateNodeOrder(Long projectId, List<UpdateNodOrderRequest> request) {

        Long loginUser = SecurityUtil.getCurrentUserIdOrThrow();
        projectNodeValidator.validateLoginUserPermission(projectId, loginUser);

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
                eventPublisher.publishEvent(new ProjectNodeUpdatedEvent(projectId, req.projectNodeId(), node.getTitle(), "순서"));
            }
        });
    }

    /**
     * 프로젝트 노드의 정보를 수정합니다.
     * @param projectId 프로젝트 ID
     * @param nodeId  노드 ID
     * @param request  수정 요청 정보
     */
    public CreateNodeResponse updateNode(Long projectId, Long nodeId, UpdateNodeRequest request) {

        ProjectNode original = getOriginalAndSaveHistory(projectId, nodeId);

        String beforeTitle = original.getTitle();
        String beforeDescription = original.getDescription();

        original.update(request);

        boolean titleChanged = !beforeTitle.equals(original.getTitle());
        boolean descChanged = !beforeDescription.equals(original.getDescription());
        eventPublisher.publishEvent(new ProjectNodeUpdatedEvent(
                projectId,
                nodeId,
                original.getTitle(),
                buildChangedDesc(titleChanged, descChanged)
        ));

        return CreateNodeResponse.from(original);
    }

    /**
     * confirm의 상태 값을 변경합니다.
     *
     * @param projectId 프로젝트 ID
     * @param nodeId    노드 ID
     * @param request 변경할 상태값
     */
    public void updateConfirm(Long projectId, Long nodeId, ClientStatusRequest request) {

        ProjectNode original = getOriginalAndSaveHistory(projectId, nodeId);

        updateNodeAndConfirmStatus(original, request);
        sendConfirmStatusNotify(projectId, nodeId, original.getTitle(), request.confirmStatus());
    }

    /**
     * 노드 Confirm 상태에 따라 적잘한 알림을 보냅니다.
     */
    private void sendConfirmStatusNotify(Long projectId, Long nodeId, String title, ConfirmStatus confirmStatus) {
        switch (confirmStatus) {
            case PENDING -> eventPublisher.publishEvent(new ProjectNodeReviewRequestedEvent(projectId, nodeId, title, "관리자가 검토를 요청했습니다."));
            case APPROVED -> eventPublisher.publishEvent(new ProjectNodeApprovedEvent(projectId, nodeId, title, "프로젝트 노드가 승인되었습니다."));
            case REJECTED -> eventPublisher.publishEvent(new ProjectNodeRejectedEvent(projectId, nodeId, title, "프로젝트 노드가 반려되었습니다."));
        }
    }

    /**
     * 수정 전 원본 엔티티를 조회하고, 변경 전 상태를 히스토리에 기록합니다.
     */
    private ProjectNode getOriginalAndSaveHistory (Long projectId, Long nodeId) {

        Long loginUser = SecurityUtil.getCurrentUserIdOrThrow();
        projectNodeValidator.validateProjectMemberPermission(projectId, loginUser);

        ProjectNode original = projectNodeService.findByIdAndProjectId(nodeId, projectId);
        NodeSnapshot snapshot = NodeSnapshot.from(original);
        historyRecorder.recordHistory(HistoryType.PROJECT_NODE, nodeId, ActionType.UPDATE, snapshot);

        return original;
    }

    /**
     * 노드의 현재 상태를 변경합니다.
     */
    private void updateNodeStatus(ProjectNode node, NodeStatus nodeStatus) {

        StatusValidator.validateStatusChange(node.getNodeStatus(), nodeStatus);
        node.updateNodeStatus(nodeStatus);
    }

    /**
     * 변경하려는 상태 값에 따라 알맞은 상태로 변경합니다.
     */
    private void updateNodeAndConfirmStatus(ProjectNode node, ClientStatusRequest confirmStatus) {
        switch (confirmStatus.confirmStatus()) {
            case PENDING -> {
                if (!node.getNodeStatus().equals(NodeStatus.PENDING_REVIEW)) {
                    updateNodeStatus(node, NodeStatus.PENDING_REVIEW);
                }
                node.updateConfirmStatus(confirmStatus.confirmStatus());
            }
            case APPROVED -> {
                updateNodeStatus(node, NodeStatus.DONE);
                node.updateConfirmStatus(confirmStatus.confirmStatus());
            }
            case REJECTED -> {
                node.updateConfirmStatus(confirmStatus.confirmStatus());
                node.updateReject(confirmStatus.rejectMessage());
            }
        }
    }

    private String buildChangedDesc(boolean titleChanged, boolean descChanged) {
        StringBuilder sb = new StringBuilder();
        if (titleChanged) sb.append("제목, ");
        if (descChanged) sb.append("설명, ");
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }
}
