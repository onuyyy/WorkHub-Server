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
@Table(name = "check_list_template")
@Entity
public class CheckListTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "item_title", length = 50, nullable = false)
    private String itemTitle;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "check_list_item_id")
    private Long checkListItemId;


}
