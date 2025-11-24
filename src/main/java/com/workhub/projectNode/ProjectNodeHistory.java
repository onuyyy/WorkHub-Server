package com.workhub.projectNode;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_node_history")
public class ProjectNodeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_node_history_id")
    private Long projectNodeHistoryId;

    @Column(name = "status")
    private Status status;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "project_node_id")
    private Long projectNodeId;

}
