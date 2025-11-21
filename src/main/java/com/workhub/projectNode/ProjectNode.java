package com.workhub.projectNode;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_node")
public class ProjectNode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_node_id")
    private Long ProjectNodeId;

    @Column(name = "title", length = 50)
    private String Title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String Description;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_status")
    private NodeStatus nodeStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirm_status")
    private ConfirmStatus confirmStatus;

    @Column(name = "reject_text")
    private String rejectText;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "node_order")
    private Integer nodeOrder;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;
}
