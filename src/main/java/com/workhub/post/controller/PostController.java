package com.workhub.post.controller;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.post.api.PostApi;
import com.workhub.post.dto.post.request.PostRequest;
import com.workhub.post.dto.post.request.PostUpdateRequest;
import com.workhub.post.dto.post.response.PostPageResponse;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.entity.PostType;
import com.workhub.post.service.post.CreatePostService;
import com.workhub.post.service.post.DeletePostService;
import com.workhub.post.service.post.ReadPostService;
import com.workhub.post.service.post.UpdatePostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/posts")
public class PostController implements PostApi {
    private final CreatePostService createPostService;
    private final ReadPostService readPostService;
    private final UpdatePostService updatePostService;
    private final DeletePostService deletePostService;

    /**
     * 프로젝트/노드별 게시글을 생성한다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId    프로젝트 단계 식별자
     * @param request   게시글 생성 요청
     * @return ApiResponse 래퍼로 감싼 생성 결과
     */
    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestPart("data") PostRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostResponse created = createPostService.create(projectId, nodeId, getUserId(userDetails), request, files);
        return ApiResponse.created(created, "게시글이 생성되었습니다.");
    }

    /**
     * 프로젝트/노드별 게시글 목록을 조회한다.
     *
     * @param projectId 프로젝트 식별자 (추후 검증 로직에 사용 예정)
     * @param nodeId    프로젝트 단계 식별자 (추후 검증 로직에 사용 예정)
     * @return ApiResponse<PostPageResponse>
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<PostPageResponse>> getPosts(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PostType postType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        /**
         * 검색 조건과 Pageable 정보를 기반으로 게시글 목록을 조회한다.
         */
        PostPageResponse response = readPostService.search(projectId, nodeId, keyword, postType, pageable);

        return ApiResponse.success(response, "게시글 목록 조회에 성공했습니다.");
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long projectId,
                                                             @PathVariable Long nodeId,
                                                             @PathVariable Long postId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostResponse response = readPostService.findById(projectId, nodeId, postId);
        return ApiResponse.success(response, "게시글 조회에 성공했습니다.");
    }

    /**
     * 게시글을 수정한다. 프로젝트/노드 검증은 추후 추가 예정이며, 현재는 postId 기준으로만 수정한다.
     */
    @Override
    @PatchMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(@PathVariable Long projectId,
                                                                @PathVariable Long nodeId,
                                                                @PathVariable Long postId,
                                                                @Valid @RequestBody PostUpdateRequest request,
                                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        PostResponse updated = updatePostService.update(projectId, nodeId, postId, getUserId(userDetails), request);
        return ApiResponse.success(updated, "게시물 수정에 성공했습니다.");
    }

    /**
     * 게시글을 삭제한다. 프로젝트/노드 검증은 추후 추가된다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId    프로젝트 단계 식별자
     * @param postId    게시글 식별자
     * @return 삭제 결과 메시지
     */
    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Object>> deletePost(@PathVariable Long projectId,
                                                          @PathVariable Long nodeId,
                                                          @PathVariable Long postId,
                                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        deletePostService.delete(projectId, nodeId, postId, getUserId(userDetails));
        return ApiResponse.success(null, "게시물 삭제에 성공했습니다.");
    }

    /**
     * 인증 객체의 사용자 ID를 추출하며, 비로그인 상태는 즉시 차단한다.
     *
     * @param userDetails 인증 정보
     * @return 사용자 ID
     */
    private Long getUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        }
        return userDetails.getUserId();
    }
}
