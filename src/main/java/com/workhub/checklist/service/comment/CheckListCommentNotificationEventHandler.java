package com.workhub.checklist.service.comment;

import com.workhub.checklist.event.CheckListCommentCreatedEvent;
import com.workhub.global.notification.NotificationPublisher;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CheckListCommentNotificationEventHandler {

    private final NotificationPublisher notificationPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCommentCreated(CheckListCommentCreatedEvent event) {
        Set<Long> receivers = new HashSet<>();
        if (event.checklistOwnerId() != null) {
            receivers.add(event.checklistOwnerId());
        }
        if (event.parentCommentAuthorId() != null) {
            receivers.add(event.parentCommentAuthorId());
        }
        if (event.authorId() != null) {
            receivers.remove(event.authorId()); // 자기 자신 제외
        }
        if (receivers.isEmpty()) {
            return;
        }

        String url = buildUrl(event.projectId(), event.nodeId());
        String title = "체크리스트 댓글이 등록되었습니다.";
        String content = event.content() == null ? "새 댓글이 추가되었습니다." : event.content();

        notificationPublisher.publishProjectNode(
                receivers,
                NotificationType.CHECKLIST_COMMENT_CREATED,
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
