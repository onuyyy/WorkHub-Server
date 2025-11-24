package com.workhub.projectNode;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_node_history")
public class ProjectNodeHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_node_history_id")
    private Long projectNodeHistoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "changed_by")
    private Long changedBy;

    @Column(name = "project_node_id")
    private Long projectNodeId;

}
