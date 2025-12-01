package com.workhub.checklist.entity;

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
@Table(name = "check_list_item_comment_file")
@Entity
public class CheckListItemCommentFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_file_id")
    private Long commentFileId;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_order")
    private Integer fileOrder;

    @Column(name = "cl_comment_id")
    private Long clCommentId;
}
