package com.workhub.projectNotification.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project_notification")
@Entity
public class ProjectNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_notification_id")
    private Long projectNotificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "notification_content", length = 100)
    private String notificationContent;

    @Column(name = "related_url", length = 50)
    private String relatedUrl;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "sender_user_id")
    private Long senderUserId;

    @Column(name = "sender_name", length = 50)
    private String senderName;

    @Column(name = "sender_profile_img")
    private String senderProfileImg;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "project_node_id")
    private Long projectNodeId;

    @Column(name = "cs_qna_id")
    private Long csQnaId;

    @Column(name = "cs_post_id")
    private Long csPostId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "comment_id")
    private Long commentId;

    public void markRead() {
        this.readAt = LocalDateTime.now();
    }

    /**
     * 알림 엔티티 생성 팩토리.
     * 서비스 레이어에서는 빌더를 직접 사용하지 않고 이 메서드로 생성한다.
     */
    public static ProjectNotification of(
            Long userId,
            NotificationType type,
            String title,
            String content,
            String relatedUrl,
            Long projectId,
            Long projectNodeId,
            Long postId,
            Long commentId,
            Long csQnaId,
            Long csPostId,
            Long senderUserId,
            String senderName,
            String senderProfileImg
    ) {
        return ProjectNotification.builder()
                .userId(userId)
                .senderUserId(senderUserId)
                .senderName(senderName)
                .senderProfileImg(senderProfileImg)
                .notificationType(type)
                .title(title)
                .notificationContent(content)
                .relatedUrl(relatedUrl)
                .projectId(projectId)
                .projectNodeId(projectNodeId)
                .postId(postId)
                .commentId(commentId)
                .csQnaId(csQnaId)
                .csPostId(csPostId)
                .build();
    }
}
