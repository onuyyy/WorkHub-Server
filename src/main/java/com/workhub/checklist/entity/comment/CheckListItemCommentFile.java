package com.workhub.checklist.entity.comment;

import com.workhub.checklist.dto.comment.CheckListCommentFileRequest;
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

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "file_order", nullable = false)
    private Integer fileOrder;

    @Column(name = "cl_comment_id")
    private Long clCommentId;

    public static CheckListItemCommentFile of(Long clCommentId, CheckListCommentFileRequest request) {
        return CheckListItemCommentFile.builder()
                .clCommentId(clCommentId)
                .fileName(request.fileName())
                .fileOrder(request.fileOrder())
                .build();
    }
}
