package com.workhub.cs.service.csQna;

import com.workhub.global.notification.NotificationPublisher;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * CS QnA 알림 전담 서비스.
 */
@Service
@RequiredArgsConstructor
public class CsQnaNotificationService {

    private final NotificationPublisher notificationPublisher;

    /**
     * QnA 댓글 작성 시: 게시물 작성자 + (대댓글이면) 부모 댓글 작성자에게 알림.
     * 부모가 아니고 이전 댓글들이 있다면, 마지막 댓글 작성자 한 명에게 알림을 보내려면 callers가 전달해야 함.
     */
    public void notifyCreated(Long projectId, Long csPostId, Long csQnaId,
                              Long postAuthorId, Long parentCommentAuthorId) {
        Set<Long> receivers = new HashSet<>();
        if (postAuthorId != null) receivers.add(postAuthorId);
        if (parentCommentAuthorId != null) receivers.add(parentCommentAuthorId);
        if (receivers.isEmpty()) return;
        String url = "/api/v1/projects/" + projectId + "/csPosts/" + csPostId + "/qnas";
        notificationPublisher.publishCsQna(receivers, NotificationType.CS_QNA_CREATED,
                "QnA 댓글", "QnA에 댓글이 등록되었습니다.", url, csQnaId);
    }
}
