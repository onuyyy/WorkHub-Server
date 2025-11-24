package com.workhub.cs.entity;

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
@Table(name = "cs_post_file")
@Entity
public class CsPostFile {

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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "cs_post_id")
    private Long csPostId;
}