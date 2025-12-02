package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeletePostService {

    private final PostService postService;
    private final ProjectService projectService;

    /**
     * 게시글 삭제 시 프로젝트/노드 검증과 작성자 권한을 체크한다.
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param postId 게시글 ID
     * @param userId 요청자 ID
     */
    public void delete(Long projectId, Long nodeId, Long postId, Long userId) {
        projectService.validateProject(projectId);
        Post target = postService.findById(postId);
        postService.validateNode(target, nodeId);
        if (target.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_POST);
        }
        if (!target.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_POST_DELETE);
        }
        target.markDeleted();
    }
}
