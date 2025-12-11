package com.workhub.global.notification;

import com.workhub.project.entity.ProjectClientMember;
import com.workhub.project.entity.ProjectDevMember;
import com.workhub.project.service.ProjectService;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostComment;
import com.workhub.post.service.comment.CommentService;
import com.workhub.post.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 알림 대상 userId를 계산하는 유틸.
 */
@Component
@RequiredArgsConstructor
public class NotificationTargetFinder {

    private final ProjectService projectService;
    private final PostService postService;
    private final CommentService commentService;

    /**
     * 프로젝트 전체 멤버(고객 + 개발) userId 집합을 반환한다.
     */
    public Set<Long> findAllMembersOfProject(Long projectId) {
        var clients = projectService.getClientMemberByProjectIdIn(java.util.List.of(projectId));
        var devs = projectService.getDevMemberByProjectIdIn(java.util.List.of(projectId));
        return Stream.concat(
                        clients.stream().map(ProjectClientMember::getUserId),
                        devs.stream().map(ProjectDevMember::getUserId))
                .collect(Collectors.toSet());
    }

    /**
     * 게시글 작성자 userId 단일 조회.
     */
    public Long findPostAuthor(Long postId) {
        Post post = postService.findById(postId);
        return post.getUserId();
    }

    /**
     * 댓글 작성자 userId 단일 조회.
     */
    public Long findCommentAuthor(Long commentId) {
        PostComment c = commentService.findById(commentId);
        return c.getUserId();
    }
}
