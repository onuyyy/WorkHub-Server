package com.workhub.post.entity;

import com.workhub.global.entity.BaseTimeEntity;
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
    private Long postFileId;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "file_name", nullable = false)
    private String fileName; // 파일 이름

    @Column(name = "post_id", nullable = false)
    private Long post; // 게시글

    @Column(name = "file_order")
    private Integer fileOrder; // 파일 순서
}
