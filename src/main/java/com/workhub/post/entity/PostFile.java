package com.workhub.post.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.dto.post.request.PostFileRequest;
import com.workhub.post.dto.post.request.PostFileUpdateRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "post_file")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_file_id")
    private Long postFileId;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "file_order")
    private Integer fileOrder;

    public static PostFile of(Long postId, PostFileRequest request) {
        return PostFile.builder()
                .postId(postId)
                .fileName(request.fileName())
                .fileOrder(validateOrder(request.fileOrder()))
                .build();
    }

    public static PostFile of(Long postId, PostFileUpdateRequest request) {
        return PostFile.builder()
                .postId(postId)
                .fileName(request.fileName())
                .fileOrder(validateOrder(request.fileOrder()))
                .build();
    }

    public void markDeleted(){ markDeletedNow();}

    public void updateOrder(Integer newOrder){ this.fileOrder = validateOrder(newOrder); }

    private static Integer validateOrder(Integer fileOrder) {
        if (fileOrder == null || fileOrder < 0) {
            throw new BusinessException(ErrorCode.INVALID_POST_FILE_ORDER);
        }
        return fileOrder;
    }

}
