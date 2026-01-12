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
    @Column(name = "node_category")
    private NodeCategory nodeCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "node_status")
    private NodeStatus nodeStatus;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

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

    @Column(name = "confirm_user_id")
    private Long confirmUserId;

    @Column(name = "developer_user_id")
    private Long developerUserId;

    public void incrementNodeOrder() {
        this.nodeOrder++;
    }

    public void updateReject(String msg) { this.rejectText = msg; }

    public void updateNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public void updateConfirmStatus(ConfirmStatus confirmStatus) { this.confirmStatus = confirmStatus; }

    public void updateNodeOrder(Integer nodeOrder) {
        this.nodeOrder = nodeOrder;
    }

    public void update(UpdateNodeRequest request) {
        if(request.title() != null){
            this.title = request.title();
        }
        if(request.nodeCategory() != null){
            this.nodeCategory = request.nodeCategory();
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
        if(request.developerUserId() != null){
            this.developerUserId = request.developerUserId();
        }
    }

    public void markDeleted() {
        this.nodeStatus = NodeStatus.DELETED;
        markDeletedNow();
    }

    public static ProjectNode of(Long projectId, CreateNodeRequest request, Integer nodeOrder) {
        return ProjectNode.builder()
                .title(request.title())
                .nodeCategory(request.nodeCategory())
                .description(request.description())
                .nodeStatus(NodeStatus.NOT_STARTED)
                .contractStartDate(request.startDate())
                .contractEndDate(request.endDate())
                .developerUserId(request.developerUserId())
                .nodeOrder(nodeOrder)
                .projectId(projectId)
                .build();
    }
}
