package com.workhub.post.entity;

import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "post_comment")
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "parent_comment_id")
    private Long parentCommentId;

    public static PostComment of(Long postId, Long userId, Long parentCommentId, String content) {
        return PostComment.builder()
                .postId(postId)
                .userId(userId)
                .parentCommentId(parentCommentId)
                .content(content)
                .build();
    }

    public void updateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_COMMENT_CONTENT);
        }
        this.content = content;
    }
    public void markDeleted(){
        markDeletedNow();}
}
