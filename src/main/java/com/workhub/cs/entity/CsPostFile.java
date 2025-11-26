package com.workhub.cs.entity;

import com.workhub.cs.dto.CsPostFileRequest;
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

    @Column(name = "cs_post_id")
    private Long csPostId;

    public static CsPostFile of(Long csPostId, CsPostFileRequest request) {
        return CsPostFile.builder()
                .csPostId(csPostId)
                .fileUrl(request.fileUrl())
                .fileName(request.fileName())
                .fileOrder(request.fileOrder())
                .build();
    }
}
