package com.workhub.projectNode.service;

import
        com.workhub.global.notification.NotificationPublisher;
import com.workhub.global.notification.NotificationTargetFinder;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 프로젝트 노드 알림 전담 서비스.
 * - 생성/정보 변경/상태 변경 시 프로젝트 구성원 전체에게 알림을 발행한다.
 */
@Service
@RequiredArgsConstructor
public class ProjectNodeNotificationService {

    private final NotificationPublisher notificationPublisher;
    private final NotificationTargetFinder targetFinder;

    /**
     * 노드 생성 알림.
     */
    public void notifyCreated(Long projectId, Long nodeId, String title) {
        Set<Long> receivers = targetFinder.findAllMembersOfProject(projectId);
        if (receivers.isEmpty()) return;
        String url = buildUrl(projectId, nodeId);
        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.PROJECT_NODE_CREATED,
                title,
                "프로젝트 노드가 생성되었습니다.",
                url,
                nodeId
        );
    }

    /**
     * 노드 정보/상태 변경 알림.
     * @param changedDesc 변경된 필드 설명(예: "제목, 상태")
     */
    public void notifyUpdated(Long projectId, Long nodeId, String title, String changedDesc) {
        Set<Long> receivers = targetFinder.findAllMembersOfProject(projectId);
        if (receivers.isEmpty()) return;
        String url = buildUrl(projectId, nodeId);
        String content = changedDesc == null || changedDesc.isBlank()
                ? "프로젝트 노드가 수정되었습니다."
                : "프로젝트 노드가 수정되었습니다. (" + changedDesc + ")";

        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.PROJECT_NODE_UPDATED,
                title,
                content,
                url,
                nodeId
        );
    }

    public void notifyPending(Long projectId, Long nodeId, String title, String changedDesc) {
        Set<Long> receivers = targetFinder.findAllClientMembersOfProject(projectId);
        if (receivers.isEmpty()) return;
        String url = buildUrl(projectId, nodeId);

        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.REVIEW_REQUEST,
                title,
                changedDesc,
                url,
                nodeId
        );
    }

    public void notifyApproved(Long projectId, Long nodeId, String title, String changedDesc) {
        Set<Long> receivers = targetFinder.findAllDevMembersOfProject(projectId);
        if (receivers.isEmpty()) return;
        String url = buildUrl(projectId, nodeId);

        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.REVIEW_COMPLETED,
                title,
                changedDesc,
                url,
                nodeId
        );
    }

    public void notifyRejected(Long projectId, Long nodeId, String title, String changedDesc) {
        Set<Long> receivers = targetFinder.findAllDevMembersOfProject(projectId);
        if (receivers.isEmpty()) return;
        String url = buildUrl(projectId, nodeId);

        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.REVIEW_REJECTED,
                title,
                changedDesc,
                url,
                nodeId
        );
    }

    private String buildUrl(Long projectId, Long nodeId) {
        return "/projects/" + projectId + "/nodes/" + nodeId;
    }
}
