package com.workhub.post.service.comment;

import com.workhub.global.port.AuthorLookupPort;
import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.post.entity.PostComment;
import com.workhub.post.service.PostValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCommentService {
    private final CommentService commentService;
    private final PostValidator postValidator;
    private final AuthorLookupPort authorLookupPort;

    public Page<CommentResponse> findComment(Long projectId, Long postId, Pageable pageable) {
        postValidator.validatePostToProject(postId, projectId);

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

        Set<Long> userIds = allComments.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNameMap = authorLookupPort.findByUserIds(userIds.stream().toList()).entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue().userName()));

        return topLevelComments.stream()
                .map(parent -> buildCommentWithChildren(parent, childrenMap, userNameMap))
                .collect(Collectors.toList());
    }

    /**
     * 재귀적으로 댓글과 그 자식 댓글들을 구성한다.
     * @param postComment 현재 댓글
     * @param childrenMap 부모 ID를 키로 하는 자식 댓글 맵
     * @return 자식 댓글을 포함한 CsQnaResponse
     */
    private CommentResponse buildCommentWithChildren(PostComment postComment, Map<Long, List<PostComment>> childrenMap, Map<Long, String> userNameMap) {
        List<PostComment> children = childrenMap.getOrDefault(postComment.getCommentId(), new ArrayList<>());

        List<CommentResponse> childResponses = children.stream()
                .map(child -> buildCommentWithChildren(child, childrenMap, userNameMap))
                .collect(Collectors.toList());

        String userName = userNameMap.get(postComment.getUserId());
        return CommentResponse.from(postComment, userName).withChildren(childResponses);
    }

}
