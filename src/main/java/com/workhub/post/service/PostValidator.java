package com.workhub.post.service;

import com.workhub.post.entity.Post;
import com.workhub.post.service.post.PostService;
import com.workhub.project.service.ProjectService;
import com.workhub.projectNode.service.ProjectNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 게시글/노드/프로젝트 간 소속 관계를 검증하는 Validator.
 */
@Component
@RequiredArgsConstructor
public class PostValidator {
    private final ProjectNodeService projectNodeService;
    private final ProjectService projectService;
    private final PostService postService;

    /**
     * 노드가 프로젝트에 속하고, 프로젝트 상태가 유효한지 검증한다.
     *
     * @param nodeId 노드 ID
     * @param projectId 프로젝트 ID
     */
    public void validateNodeAndProject(Long nodeId, Long projectId) {
        projectNodeService.validateNodeToProject(nodeId, projectId);
        projectService.validateProject(projectId);
    }

    /**
     * 게시글이 프로젝트에 속하는지 검증하고 게시글을 반환한다.
     * 게시글의 노드 정보를 기반으로 프로젝트 소속을 확인한다.
     *
     * @param postId 게시글 ID
     * @param projectId 프로젝트 ID
     * @return 검증된 게시글 엔티티
     */
    public Post validatePostToProject(Long postId, Long projectId) {
        Post post = postService.findById(postId);
        projectNodeService.validateNodeToProject(post.getProjectNodeId(), projectId);
        return post;
    }
}

