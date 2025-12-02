package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.post.record.request.PostUpdateRequest;
import com.workhub.post.record.response.PostResponse;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdatePostService {

    private final PostService postService;
    private final ProjectService projectService;

    /**
     * 프로젝트와 노드 일치 여부, 작성자 권한을 검증한 뒤 게시글을 수정한다.
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param postId 게시글 ID
     * @param userId 요청자 ID
     * @param request 수정 요청
     * @return 수정된 게시글
     */
    public PostResponse update(Long projectId, Long nodeId, Long postId, Long userId, PostUpdateRequest request) {
        projectService.validateProject(projectId);
        Post target = postService.findById(postId);
        postService.validateNode(target, nodeId);
        if (!target.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_POST_UPDATE);
        }
        target.update(request);
        return PostResponse.from(target);
    }
}
