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

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "project_node_id")
    private Long projectNodeId;

    @Column(name = "cs_qna_id")
    private Long csQnaId;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "comment_id")
    private Long commentId;
}
