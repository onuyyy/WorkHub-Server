package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.record.response.PostPageResponse;
import com.workhub.post.record.response.PostResponse;
import com.workhub.post.record.response.PostSummaryResponse;
import com.workhub.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadPostService {

    private final PostService postService;
    private final ProjectService projectService;

    /**
     * 프로젝트/노드 범위 내 게시글을 단건 조회한다.
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param userId 인증 사용자 ID
     * @param postId 게시글 ID
     * @return 조회된 게시글
     */
    public PostResponse findById(Long projectId, Long nodeId, Long userId, Long postId) {
        ensureAuthenticated(userId);
        projectService.validateProject(projectId);
        Post post = postService.findById(postId);
        postService.validateNode(post, nodeId);
        return PostResponse.from(post);
    }

    /**
     * 프로젝트/노드 범위 내 게시글 목록을 검색한다.
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param userId 인증 사용자 ID
     * @param keyword 검색 키워드
     * @param postType 게시글 타입
     * @param hashTag 해시태그 필터
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    public PostPageResponse search(Long projectId,
                                   Long nodeId,
                                   Long userId,
                                   String keyword,
                                   PostType postType,
                                   HashTag hashTag,
                                   Pageable pageable) {
        ensureAuthenticated(userId);
        projectService.validateProject(projectId);
        Page<Post> page = postService.search(nodeId, keyword, postType, hashTag, pageable);
        List<PostSummaryResponse> posts = page.getContent().stream()
                .map(PostSummaryResponse::from)
                .toList();
        return PostPageResponse.of(posts, page);
    }

    /**
     * 인증되지 않은 요청을 선제적으로 차단한다.
     *
     * @param userId 인증 사용자 ID
     */
    private void ensureAuthenticated(Long userId) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        }
    }
}
