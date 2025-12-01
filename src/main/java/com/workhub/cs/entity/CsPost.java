package com.workhub.cs.entity;

import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostUpdateRequest;
import com.workhub.global.entity.BaseTimeEntity;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "cs_post")
@Entity
public class CsPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cs_post_id")
    private Long csPostId;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;

    public static CsPost of(Long projectId, Long userId, CsPostRequest request) {
        return CsPost.builder()
                .projectId(projectId)
                .userId(userId)
                .title(request.title())
                .content(request.content())
                .build();
    }

    public static CsPost of(Long projectId, CsPostUpdateRequest request) {
        return CsPost.builder()
                .title(request.title())
                .content(request.content())
                .build();
    }

    public void updateTitle(String newTitle) {
        if (newTitle == null && newTitle.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CS_POST_TITLE);
        }
        this.title = newTitle;
    }

    public void markDeleted() {
        markDeletedNow();
    }

    public void updateContent(String newContent) {
        if (newContent == null && newContent.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CS_POST_CONTENT);
        }
        this.content = newContent;
    }

    public void validateProject(Long requestProjectId) {
        if (!Objects.equals(this.projectId, requestProjectId)) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_CS_POST);
        }
    }

}
