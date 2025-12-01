package com.workhub.cs.entity;

import com.workhub.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "cs_qna")
@Entity
public class CsQna extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cs_qna_id")
    private Long csQnaId;

    @Column(name = "qna_content", columnDefinition = "TEXT")
    private String qnaContent;

    @Column(name = "cs_post_id")
    private Long csPostId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "parent_qna_id")
    private Long parentQnaId;
}
