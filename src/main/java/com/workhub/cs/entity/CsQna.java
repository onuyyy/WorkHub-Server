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
@Table(name = "cs_qna")
@Entity
public class CsQna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cs_qna_id")
    private Long csQnaId;

    @Column(name = "qna_content", columnDefinition = "TEXT")
    private String qnaContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "cs_post_id")
    private Long csPostId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "parent_qna_id")
    private Long parentQnaId;
}