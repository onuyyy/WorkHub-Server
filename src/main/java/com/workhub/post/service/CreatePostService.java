package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.record.request.PostFileRequest;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.response.PostResponse;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        }
        if (parentPostId != null) {
            Post parent = postService.findById(parentPostId);
            postService.validateNode(parent, projectNodeId);
        }

        Post savedPost = postService.save(Post.of(projectNodeId, userId, parentPostId, request));
        List<PostFile> savedFiles = savePostFiles(savedPost.getPostId(), request.files());

        return PostResponse.from(savedPost, savedFiles);
    }

    /**
     * 첨부 파일 요청을 엔티티로 변환해 한번에 저장한다.
     *
     * @param postId 게시글 ID
     * @param fileRequests 첨부 파일 요청 목록
     * @return 저장된 파일 목록, 없으면 빈 리스트
     */
    private List<PostFile> savePostFiles(Long postId, List<PostFileRequest> fileRequests) {
        if (fileRequests == null || fileRequests.isEmpty()) {
            return List.of();
        }
        List<PostFile> files = fileRequests.stream()
                .map(request -> PostFile.of(postId, request))
                .toList();
        return postService.savePostFiles(files);
    }
}
