package com.workhub.post.service.post;

import com.workhub.global.notification.NotificationPublisher;
import com.workhub.global.notification.NotificationTargetFinder;
import com.workhub.post.entity.Post;
import com.workhub.projectNotification.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostNotificationService {
    private final NotificationTargetFinder notificationTargetFinder;
    private final NotificationPublisher notificationPublisher;

    /**
     * 프로젝트 참여자 모두에게 게시글 생성 알림을 전송한다.
     *
     * @param projectId 프로젝트 ID
     * @param post 생성된 게시글
     */
    protected void notifyPostCreated(Long projectId, Post post) {
        Set<Long> receivers = notificationTargetFinder.findAllMembersOfProject(projectId);
        if (receivers.isEmpty()) {
            return;
        }
        String relatedUrl = "/projects/" + projectId + "/nodes/" + post.getProjectNodeId() + "/posts/" + post.getPostId();
        notificationPublisher.publishPost(
                receivers,
                NotificationType.POST_CREATED,
                post.getTitle(),
                "게시글이 생성되었습니다.",
                relatedUrl,
                post.getPostId()
        );
    }

    /**
     * 게시글 삭제 시 프로젝트 멤버에게 알림을 전송한다.
     */
    protected void notifyPostDeleted(Long projectId, Post post) {
        Set<Long> receivers = notificationTargetFinder.findAllMembersOfProject(projectId);
        if (receivers.isEmpty()) {
            return;
        }
        String relatedUrl = "/projects/" + projectId + "/nodes/" + post.getProjectNodeId() + "/posts/" + post.getPostId();
        notificationPublisher.publishPost(
                receivers,
                NotificationType.POST_DELETED,
                post.getTitle(),
                "게시글이 삭제되었습니다.",
                relatedUrl,
                post.getPostId()
        );
    }

    /**
     * 게시글 수정 시 프로젝트 멤버에게 알림을 보낸다.
     */
    protected void notifyPostUpdated(Long projectId, Post post) {
        Set<Long> receivers = notificationTargetFinder.findAllMembersOfProject(projectId);
        if (receivers.isEmpty()) {
            return;
        }
        String relatedUrl = "/projects/" + projectId + "/nodes/" + post.getProjectNodeId() + "/posts/" + post.getPostId();
        notificationPublisher.publishPost(
                receivers,
                NotificationType.POST_UPDATED,
                post.getTitle(),
                "게시글이 수정되었습니다.",
                relatedUrl,
                post.getPostId()
        );
    }
}
