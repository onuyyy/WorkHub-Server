package com.workhub.cs.entity;

import com.workhub.cs.dto.CsPostFileRequest;
import com.workhub.cs.dto.CsPostFileUpdateRequest;
import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cs_post_file")
@Entity
public class CsPostFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cs_post_file_id")
    private Long csPostFileId;

    @Column(name = "file_url", length = 255)
    private String fileUrl;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_order")
    private Integer fileOrder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "cs_post_id")
    private Long csPostId;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static CsPostFile of(Long csPostId, CsPostFileRequest request) {
        return CsPostFile.builder()
                .csPostId(csPostId)
                .fileUrl(request.fileUrl())
                .fileName(request.fileName())
                .fileOrder(request.fileOrder())
                .build();
    }

    public static CsPostFile of(Long csPostId, CsPostFileUpdateRequest request) {
        return CsPostFile.builder()
                .csPostId(csPostId)
                .fileUrl(request.fileUrl())
                .fileName(request.fileName())
                .fileOrder(request.fileOrder())
                .build();
    }

    /**
     * 파일 삭제
     */
    public void markDeleted() {
        this.deletedAt = (LocalDateTime.now());
    }

    /**
     * 파일 순서 변경
     */
    public void updateOrder(Integer newOrder) {
        this.fileOrder = validateOrder(newOrder);
    }

    /**
     * 순서 검증
     */
    private static Integer validateOrder(Integer order) {
        if (order == null || order < 0) {
            throw new BusinessException(ErrorCode.INVALID_CS_POST_FILE_ORDER);
        }
        return order;
    }
}
