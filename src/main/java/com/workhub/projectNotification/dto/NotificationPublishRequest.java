package com.workhub.projectNotification.dto;

import com.workhub.projectNotification.entity.NotificationType;
import com.workhub.projectNotification.entity.ProjectNotification;

/**
 * 알림 발행 시 필요한 필드를 묶어 전달하는 요청 DTO.
 * 정적 팩토리 + 체이닝 메서드로 필요한 필드만 설정한다.
 */
public class NotificationPublishRequest {
    private Long receiverId;
    private NotificationType type;
    private String title;
    private String content;
    private String relatedUrl;
    private Long projectId;
    private Long projectNodeId;
    private Long postId;
    private Long commentId;
    private Long csQnaId;
    private Long csPostId;
    private Long senderUserId;

    private NotificationPublishRequest() {}

    /** 게시글 알림용 팩토리 */
    public static NotificationPublishRequest forPost(Long receiverId, NotificationType type, Long postId) {
        NotificationPublishRequest req = new NotificationPublishRequest();
        req.receiverId = receiverId;
        req.type = type;
        req.postId = postId;
        return req;
    }

    /** 댓글 알림용 팩토리 */
    public static NotificationPublishRequest forComment(Long receiverId, NotificationType type, Long commentId) {
        NotificationPublishRequest req = new NotificationPublishRequest();
        req.receiverId = receiverId;
        req.type = type;
        req.commentId = commentId;
        return req;
    }

    /** 프로젝트 알림용 팩토리 */
    public static NotificationPublishRequest forProject(Long receiverId, NotificationType type, Long projectId) {
        NotificationPublishRequest req = new NotificationPublishRequest();
        req.receiverId = receiverId;
        req.type = type;
        req.projectId = projectId;
        return req;
    }

    /** 노드 알림용 팩토리 */
    public static NotificationPublishRequest forProjectNode(Long receiverId, NotificationType type, Long projectNodeId) {
        NotificationPublishRequest req = new NotificationPublishRequest();
        req.receiverId = receiverId;
        req.type = type;
        req.projectNodeId = projectNodeId;
        return req;
    }

    /** CS QnA 알림용 팩토리 */
    public static NotificationPublishRequest forCsQna(Long receiverId, NotificationType type, Long csQnaId) {
        NotificationPublishRequest req = new NotificationPublishRequest();
        req.receiverId = receiverId;
        req.type = type;
        req.csQnaId = csQnaId;
        return req;
    }

    /** CS Post 알림용 팩토리 */
    public static NotificationPublishRequest forCsPost(Long receiverId, NotificationType type, Long csPostId) {
        NotificationPublishRequest req = new NotificationPublishRequest();
        req.receiverId = receiverId;
        req.type = type;
        req.csPostId = csPostId;
        return req;
    }

    public NotificationPublishRequest title(String title) { this.title = title; return this; }
    public NotificationPublishRequest content(String content) { this.content = content; return this; }
    public NotificationPublishRequest relatedUrl(String relatedUrl) { this.relatedUrl = relatedUrl; return this; }
    public NotificationPublishRequest projectId(Long projectId) { this.projectId = projectId; return this; }
    public NotificationPublishRequest projectNodeId(Long projectNodeId) { this.projectNodeId = projectNodeId; return this; }
    public NotificationPublishRequest postId(Long postId) { this.postId = postId; return this; }
    public NotificationPublishRequest commentId(Long commentId) { this.commentId = commentId; return this; }
    public NotificationPublishRequest csQnaId(Long csQnaId) { this.csQnaId = csQnaId; return this; }
    public NotificationPublishRequest csPostId(Long csPostId) { this.csPostId = csPostId; return this; }
    public NotificationPublishRequest senderUserId(Long senderUserId) { this.senderUserId = senderUserId; return this; }

    public Long receiverId() { return receiverId; }
    public NotificationType type() { return type; }
    public String title() { return title; }
    public String content() { return content; }
    public String relatedUrl() { return relatedUrl; }
    public Long projectId() { return projectId; }
    public Long projectNodeId() { return projectNodeId; }
    public Long postId() { return postId; }
    public Long commentId() { return commentId; }
    public Long csQnaId() { return csQnaId; }
    public Long csPostId() { return csPostId; }
    public Long senderUserId() { return senderUserId; }

    public ProjectNotification toEntity() {
        return ProjectNotification.of(
                receiverId, type, title, content, relatedUrl,
                projectId, projectNodeId, postId, commentId, csQnaId, csPostId, senderUserId
        );
    }

    /** 수신자만 바꾼 새 요청을 반환한다. */
    public NotificationPublishRequest withReceiver(Long newReceiverId) {
        NotificationPublishRequest copy = new NotificationPublishRequest();
        copy.receiverId = newReceiverId;
        copy.type = this.type;
        copy.title = this.title;
        copy.content = this.content;
        copy.relatedUrl = this.relatedUrl;
        copy.projectId = this.projectId;
        copy.projectNodeId = this.projectNodeId;
        copy.postId = this.postId;
        copy.commentId = this.commentId;
        copy.csQnaId = this.csQnaId;
        copy.csPostId = this.csPostId;
        copy.senderUserId = this.senderUserId;
        return copy;
    }
}
