package com.workhub.global.notification;

import com.workhub.projectNotification.dto.NotificationPublishRequest;
import com.workhub.projectNotification.entity.NotificationType;
import com.workhub.projectNotification.service.ProjectNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * 알림 발행 공통 헬퍼.
 * - 대상이 없으면 아무 것도 하지 않는다.
 * - 연관 FK는 정확히 하나만 세팅해야 DB 제약을 통과한다.
 */
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final ProjectNotificationService notificationService;

    public void publishToUsers(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long projectId,
            Long projectNodeId,
            Long postId,
            Long commentId,
            Long csQnaId,
            Long csPostId
    ) {
        if (receivers == null || receivers.isEmpty()) {
            return;
        }
        validateExactlyOneRelated(projectId, projectNodeId, postId, commentId, csQnaId, csPostId);

        NotificationPublishRequest baseRequest = NotificationPublishRequest.forProject(null, type, projectId)
                .projectNodeId(projectNodeId)
                .postId(postId)
                .commentId(commentId)
                .csQnaId(csQnaId)
                .csPostId(csPostId)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl);
        receivers.forEach(userId -> notificationService.publish(baseRequest.withReceiver(userId)));
    }

    /**
     * 게시글 관련 알림 전용 편의 메서드.
     * @param receivers 대상 사용자
     * @param type 알림 타입
     * @param title 제목
     * @param content 내용
     * @param relatedUrl 이동 URL
     * @param postId 게시글 ID(연관 FK)
     */
    public void publishPost(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long postId
    ) {
        NotificationPublishRequest base = NotificationPublishRequest.forPost(null, type, postId)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl);
        receivers.forEach(userId -> notificationService.publish(base.withReceiver(userId)));
    }

    /**
     * 댓글 관련 알림 전용 편의 메서드.
     */
    public void publishComment(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long commentId
    ) {
        NotificationPublishRequest base = NotificationPublishRequest.forComment(null, type, commentId)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl);
        receivers.forEach(userId -> notificationService.publish(base.withReceiver(userId)));
    }

    /**
     * 프로젝트 알림 편의 메서드.
     */
    public void publishProject(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long projectId
    ) {
        NotificationPublishRequest base = NotificationPublishRequest.forProject(null, type, projectId)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl);
        receivers.forEach(userId -> notificationService.publish(base.withReceiver(userId)));
    }

    /**
     * 프로젝트 노드 알림 편의 메서드.
     */
    public void publishProjectNode(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long projectNodeId
    ) {
        NotificationPublishRequest base = NotificationPublishRequest.forProjectNode(null, type, projectNodeId)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl);
        receivers.forEach(userId -> notificationService.publish(base.withReceiver(userId)));
    }

    /**
     * CS QnA 알림 편의 메서드.
     */
    public void publishCsQna(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long csQnaId
    ) {
        NotificationPublishRequest base = NotificationPublishRequest.forCsQna(null, type, csQnaId)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl);
        receivers.forEach(userId -> notificationService.publish(base.withReceiver(userId)));
    }

    /**
     * CS Post 알림 편의 메서드.
     */
    public void publishCsPost(
            Set<Long> receivers,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long csPostId
    ) {
        NotificationPublishRequest base = NotificationPublishRequest.forCsPost(null, type, csPostId)
                .title(title)
                .content(content)
                .relatedUrl(relatedUrl);
        receivers.forEach(userId -> notificationService.publish(base.withReceiver(userId)));
    }

    /**
     * DB 체크 제약 대응: 연관 FK는 하나만 세팅되어야 한다.
     */
    private void validateExactlyOneRelated(Object... vals) {
        int count = 0;
        for (Object v : vals) {
            if (Objects.nonNull(v)) {
                count++;
            }
        }
        if (count != 1) {
            throw new IllegalArgumentException("연관 FK는 하나만 설정해야 합니다.");
        }
    }
}
