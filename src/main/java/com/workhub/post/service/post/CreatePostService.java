package com.workhub.post.service.post;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.post.PostHistorySnapshot;
import com.workhub.post.dto.post.request.PostFileRequest;
import com.workhub.post.dto.post.request.PostLinkRequest;
import com.workhub.post.dto.post.request.PostRequest;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.entity.PostLink;
import com.workhub.post.service.PostValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreatePostService {

    private final PostService postService;
    private final PostValidator postValidator;
    private final HistoryRecorder historyRecorder;

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
        postValidator.validateNodeAndProject(projectNodeId, projectId);

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
        List<PostLink> savedLinks = savePostLinks(savedPost.getPostId(), request.links());

        historyRecorder.recordHistory(HistoryType.POST, savedPost.getPostId(), ActionType.CREATE, PostHistorySnapshot.from(savedPost));

        return PostResponse.from(savedPost, savedFiles, savedLinks);
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

    /**
     * 참고 링크 요청을 엔티티로 변환해 저장한다.
     *
     * @param postId 게시글 ID
     * @param linkRequests 링크 요청 목록
     * @return 저장된 링크 목록
     */
    private List<PostLink> savePostLinks(Long postId, List<PostLinkRequest> linkRequests) {
        if (linkRequests == null || linkRequests.isEmpty()) {
            return List.of();
        }
        List<PostLink> links = linkRequests.stream()
                .map(request -> PostLink.of(postId, request.referenceLink(), request.linkDescription()))
                .toList();
        return postService.savePostLinks(links);
    }
}
