package com.workhub.checklist.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "check_list_item_comment")
@Entity
public class CheckListItemComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cl_comment_id")
    private Long clCommentId;

    @Column(name = "cl_content", columnDefinition = "TEXT")
    private String clContent;

    @Column(name = "check_list_item_id")
    private Long checkListItemId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "parent_cl_comment_id")
    private Long parentClCommentId;
}
