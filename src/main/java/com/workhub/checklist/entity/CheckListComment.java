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
@Table(name = "check_list_comment")
@Entity
public class CheckListComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cl_comment_id")
    private Long clCommentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_cl_comment_id",
            foreignKey = @ForeignKey(name = "fk_comment_parent")
    )
    private CheckListComment parent;

    @Column(name = "cl_content", columnDefinition = "TEXT")
    private String clContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "check_list_item_id")
    private Long checkListItemId;

}
