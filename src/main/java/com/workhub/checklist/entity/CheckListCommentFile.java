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
@Table(name = "check_list_comment_file")
@Entity
public class CheckListCommentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_file_id")
    private Long checkListCommentFileId;

    @Column(name = "file_url", length = 255, nullable = false)
    private String fileUrl;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_order")
    private Integer fileOrder;

    @Column(name = "cl_comment_id")
    private Long clCommentId;
}
