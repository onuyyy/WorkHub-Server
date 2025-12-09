package com.workhub.post.controller;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.response.ApiResponse;
import com.workhub.post.api.CommentApi;
import com.workhub.post.dto.comment.request.CommentRequest;
import com.workhub.post.dto.comment.request.CommentUpdateRequest;
import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.post.service.comment.CreateCommentService;
import com.workhub.post.service.comment.DeleteCommentService;
import com.workhub.post.service.comment.ReadCommentService;
import com.workhub.post.service.comment.UpdateCommentService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/posts/{postId}/comments")
public class CommentController implements CommentApi {

    private final CreateCommentService createCommentService;
    private final ReadCommentService readCommentService;
    private final UpdateCommentService updateCommentService;
    private final DeleteCommentService deleteCommentService;

    /**
     * 댓글 목록을 계층 구조로 조회한다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId    노드 식별자
     * @param postId    게시글 식별자
     * @param pageable  페이지 정보
     * @return 댓글 목록 페이지 응답
     */
    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> readComments(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponse> comments = readCommentService.findComment(projectId, postId, pageable);
        return ApiResponse.success(comments, "댓글 목록 조회에 성공했습니다.");
    }

    /**
     * 댓글을 작성한다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId    노드 식별자
     * @param postId    게시글 식별자
     * @param request   작성 요청 본문
     * @param userDetails 인증 정보
     * @return 생성된 댓글 정보
     */
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponse response = createCommentService.create(projectId, postId, getUserId(userDetails), request);
        return ApiResponse.created(response, "댓글 작성에 성공했습니다.");
    }

    /**
     * 댓글을 수정한다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId    노드 식별자
     * @param postId    게시글 식별자
     * @param commentId 댓글 식별자
     * @param request   수정 요청 본문
     * @param userDetails 인증 정보
     * @return 수정된 댓글 정보
     */
    @Override
    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CommentResponse response = updateCommentService.update(commentId, postId, getUserId(userDetails), request);
        return ApiResponse.success(response, "댓글 수정에 성공했습니다.");
    }

    /**
     * 댓글을 삭제한다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId    노드 식별자
     * @param postId    게시글 식별자
     * @param commentId 댓글 식별자
     * @param userDetails 인증 정보
     * @return 삭제된 댓글 ID
     */
    @Override
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<Long>> delete(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long result = deleteCommentService.delete(projectId, postId, commentId, getUserId(userDetails));
        return ApiResponse.success(result, "댓글 삭제에 성공했습니다.");
    }

    private Long getUserId(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.NOT_LOGGED_IN);
        }
        return userDetails.getUserId();
    }
}
