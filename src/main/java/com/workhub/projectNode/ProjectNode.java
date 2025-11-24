package com.workhub.projectNode;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_node")
public class ProjectNode extends BaseTimeEntity {
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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;
}
