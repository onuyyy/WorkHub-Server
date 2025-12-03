package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.response.PostResponse;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatePostService {

    private final PostService postService;
    private final ProjectService projectService;

    /**
     * 게시글 생성 시 프로젝트 상태와 부모 게시글 유효성을 검증한 뒤 저장한다.
     *
     * @param projectId 프로젝트 ID
     * @param projectNodeId 프로젝트 노드 ID
     * @param userId 작성자 ID
     * @param request 게시글 생성 요청
     * @return 저장된 게시글
     */
    public PostResponse create(Long projectId, Long projectNodeId, Long userId, PostRequest request) {
        projectService.validateProject(projectId);
        Long parentPostId = request.parentPostId();
        if (parentPostId != null && !postService.existsActivePost(parentPostId)) {
            throw new BusinessException(ErrorCode.PARENT_POST_NOT_FOUND);
       } else if (parentPostId != null) {
            Post parent = postService.findById(parentPostId);
            postService.validateNode(parent, projectNodeId);
            if (parent.isDeleted()) {
                throw new BusinessException(ErrorCode.ALREADY_DELETED_POST);
            }
        }
        return PostResponse.from(postService.save(Post.of(projectNodeId, userId, parentPostId, request)));
    }
}
