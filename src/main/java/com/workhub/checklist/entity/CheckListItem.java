package com.workhub.checklist.entity;

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
@Table(name = "check_list_item")
@Entity
public class CheckListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_list_item_id")
    private Long checkListItemId;

    @Column(name = "item_title", length = 50, nullable = false)
    private String itemTitle;

    @Column(name = "item_order")
    private Integer itemOrder;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "confirm")
    private Boolean confirm;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "check_list_id")
    private Long checkListId;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "user_id")
    private Long userId;
}
