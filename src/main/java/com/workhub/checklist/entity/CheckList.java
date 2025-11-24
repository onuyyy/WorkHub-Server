package com.workhub.checklist.entity;

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

    @Column(name = "check_list_title", length = 50, nullable = false)
    private String checkListTitle;

    @Column(name = "project_node_id")
    private Long projectNodeId;

}
