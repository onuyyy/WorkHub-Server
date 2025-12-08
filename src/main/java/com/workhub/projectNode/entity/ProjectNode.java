package com.workhub.projectNode.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.UpdateNodeRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
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
    private Long projectNodeId;

    @Column(name = "title", length = 50)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_status")
    private NodeStatus nodeStatus;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "confirm_status")
    private ConfirmStatus confirmStatus;

    @Column(name = "reject_text", length = 255)
    private String rejectText;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "node_order")
    private Integer nodeOrder;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;

    public void incrementNodeOrder() {
        this.nodeOrder++;
    }

    public void updateNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public void updateNodeOrder(Integer nodeOrder) {
        this.nodeOrder = nodeOrder;
    }

    public void update(UpdateNodeRequest request) {
        if(request.title() != null){
            this.title = request.title();
        }
        if(request.description() != null){
            this.description = request.description();
        }
        if(request.startDate() != null){
            this.contractStartDate = request.startDate();
        }
        if(request.endDate() != null){
            this.contractEndDate = request.endDate();
        }
        if(request.priority() != null){
            this.priority = request.priority();
        }
    }

    public void markDeleted() {
        this.nodeStatus = NodeStatus.DELETED;
        markDeletedNow();
    }

    public static ProjectNode of(Long projectId, CreateNodeRequest request, Integer nodeOrder) {
        return ProjectNode.builder()
                .title(request.title())
                .description(request.description())
                .nodeStatus(NodeStatus.NOT_STARTED)
                .contractStartDate(request.starDate())
                .contractEndDate(request.endDate())
                .priority(request.priority())
                .nodeOrder(nodeOrder)
                .projectId(projectId)
                .build();
    }
}
