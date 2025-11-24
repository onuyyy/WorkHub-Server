package com.workhub.checklist.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "check_list_comment")
@Entity
public class CheckListComment extends BaseTimeEntity {

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

    @Column(name = "check_list_item_id")
    private Long checkListItemId;

    @OneToMany(mappedBy = "parent")
    private List<CheckListComment> children = new ArrayList<>();
}
