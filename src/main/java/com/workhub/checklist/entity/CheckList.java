package com.workhub.checklist.entity;

import com.workhub.checklist.dto.checkList.CheckListCreateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "check_list")
@Entity
public class CheckList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_list_id")
    private Long checkListId;

    @Column(name = "check_list_description", length = 500, nullable = false)
    private String checkListDescription;

    @Column(name = "project_node_id")
    private Long projectNodeId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public static CheckList of(CheckListCreateRequest request, Long userId, Long projectNodeId) {
        return CheckList.builder()
                .checkListDescription(request.description())
                .projectNodeId(projectNodeId)
                .userId(userId)
                .build();
    }

    public void updateDescription(String description) {
        this.checkListDescription = description;
    }

}
