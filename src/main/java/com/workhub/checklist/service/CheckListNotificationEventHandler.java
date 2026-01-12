package com.workhub.checklist.service;

import com.workhub.checklist.event.CheckListCreatedEvent;
import com.workhub.checklist.event.CheckListItemStatusChangedEvent;
import com.workhub.checklist.event.CheckListUpdatedEvent;
import com.workhub.global.notification.NotificationPublisher;
import com.workhub.global.notification.NotificationTargetFinder;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class CheckListNotificationEventHandler {

    private final NotificationTargetFinder targetFinder;
    private final NotificationPublisher notificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCreated(CheckListCreatedEvent event) {
        Set<Long> receivers = targetFinder.findAllMembersOfProject(event.projectId());
        if (receivers.isEmpty()) return;

        String url = buildUrl(event.projectId(), event.nodeId());
        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.CHECKLIST_CREATED,
                "체크리스트가 생성되었습니다.",
                "프로젝트 체크리스트가 생성되었습니다.",
                url,
                event.nodeId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUpdated(CheckListUpdatedEvent event) {
        Set<Long> receivers = targetFinder.findAllClientMembersOfProject(event.projectId());
        if (receivers.isEmpty()) return;

        String url = buildUrl(event.projectId(), event.nodeId());
        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.CHECKLIST_UPDATED,
                "체크리스트가 업데이트되었습니다.",
                event.message(),
                url,
                event.nodeId()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onItemStatusChanged(CheckListItemStatusChangedEvent event) {
        Set<Long> receivers = targetFinder.findAllDevMembersOfProject(event.projectId());
        if (receivers.isEmpty()) return;

        String url = buildUrl(event.projectId(), event.nodeId());
        String content = String.format("체크리스트 항목 상태가 %s 으로 변경되었습니다.", event.status().name());
        String title = event.itemContent() == null ? "체크리스트 항목 상태 변경" : event.itemContent();

        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.CHECKLIST_ITEM_STATUS_CHANGED,
                title,
                content,
                url,
                event.nodeId()
        );
    }

    private String buildUrl(Long projectId, Long nodeId) {
        return "/projects/" + projectId + "/nodes/" + nodeId + "/checkLists";
    }
}
