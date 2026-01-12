package com.workhub.post.service.comment;

import com.workhub.global.notification.NotificationPublisher;
import com.workhub.global.notification.NotificationTargetFinder;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostComment;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommentNotificationService {
    private final NotificationTargetFinder notificationTargetFinder;
    private final NotificationPublisher notificationPublisher;

    protected void notifyCreated(Long projectId, Post post, PostComment comment) {
        publishComment(projectId, post, comment, "댓글이 생성되었습니다.");
    }

    /**
     * 댓글 알림 공통 처리.
     */
    private void publishComment(Long projectId, Post post, PostComment comment, String message) {
        Set<Long> receivers = notificationTargetFinder.findAllMembersOfProject(projectId);
        receivers.add(notificationTargetFinder.findPostAuthor(post.getPostId()));
        if (comment.getParentCommentId() != null) {
            receivers.add(notificationTargetFinder.findCommentAuthor(comment.getParentCommentId()));
        }
        receivers.remove(comment.getUserId());
        String relatedUrl = "/projects/" + projectId + "/nodes/" + post.getProjectNodeId()
                + "/posts/" + post.getPostId() + "/comments";
        notificationPublisher.publishComment(
                receivers,
                NotificationType.POST_COMMENT_CREATED,
                comment.getContent(),
                message,
                relatedUrl,
                comment.getCommentId()
        );
    }
}
