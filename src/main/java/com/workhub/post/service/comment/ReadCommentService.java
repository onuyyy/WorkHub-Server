package com.workhub.post.service.comment;

import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.post.entity.PostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCommentService {
    private final CommentService commentService;

    public Page<CommentResponse> findComment(Long projectId, Long postId, Pageable pageable) {
        List<PostComment> allComments = commentService.findAllByPostId(postId);

        Page<PostComment> topLevelComments = commentService.findPostWithReplies(postId, pageable);

        List<CommentResponse> hierarchicalComments = buildHierarchy(
                topLevelComments.getContent(),
                allComments
        );

        return new PageImpl<>(
                hierarchicalComments,
                pageable,
                topLevelComments.getTotalElements()
        );
    }

    /**
     * 댓글 목록을 계층 구조로 변환한다.
     * @param topLevelComments 최상위 댓글 목록
     * @param allComments 모든 댓글 목록 (부모 + 자식)
     * @return 계층 구조로 구성된 댓글 목록
     */
    private List<CommentResponse> buildHierarchy(List<PostComment> topLevelComments, List<PostComment> allComments) {
        Map<Long, List<PostComment>> childrenMap = allComments.stream()
                .filter(comment -> comment.getParentCommentId() != null)
                .collect(Collectors.groupingBy(PostComment::getParentCommentId));

        return topLevelComments.stream()
                .map(parent -> buildCommentWithChildren(parent, childrenMap))
                .collect(Collectors.toList());
    }

    /**
     * 재귀적으로 댓글과 그 자식 댓글들을 구성한다.
     * @param postComment 현재 댓글
     * @param childrenMap 부모 ID를 키로 하는 자식 댓글 맵
     * @return 자식 댓글을 포함한 CsQnaResponse
     */
    private CommentResponse buildCommentWithChildren(PostComment postComment, Map<Long, List<PostComment>> childrenMap) {
        List<PostComment> children = childrenMap.getOrDefault(postComment.getCommentId(), new ArrayList<>());

        List<CommentResponse> childResponses = children.stream()
                .map(child -> buildCommentWithChildren(child, childrenMap))
                .collect(Collectors.toList());

        return CommentResponse.from(postComment).withChildren(childResponses);
    }

}
