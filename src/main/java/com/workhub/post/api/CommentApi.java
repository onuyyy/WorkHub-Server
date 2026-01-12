package com.workhub.post.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.post.dto.comment.request.CommentRequest;
import com.workhub.post.dto.comment.request.CommentUpdateRequest;
import com.workhub.post.dto.comment.response.CommentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "게시글 댓글 API")
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/posts/{postId}/comments")
public interface CommentApi {

    @Operation(
            summary = "댓글 목록 조회",
            description = "게시글에 달린 댓글 목록을 계층 구조로 조회합니다.",
            parameters = {
                    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "프로젝트 식별자", required = true),
                    @Parameter(name = "nodeId", in = ParameterIn.PATH, description = "노드 식별자", required = true),
                    @Parameter(name = "postId", in = ParameterIn.PATH, description = "게시글 식별자", required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "댓글 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CommentResponse.class))
            )
    })
    @GetMapping
    ResponseEntity<ApiResponse<Page<CommentResponse>>> readComments(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(
            summary = "댓글 작성",
            description = "게시글에 댓글을 작성합니다."
    )
    @PostMapping
    ResponseEntity<ApiResponse<CommentResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal com.workhub.global.security.CustomUserDetails userDetails
    );

    @Operation(
            summary = "댓글 수정",
            description = "댓글 내용을 수정합니다."
    )
    @PatchMapping("/{commentId}")
    ResponseEntity<ApiResponse<CommentResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal com.workhub.global.security.CustomUserDetails userDetails
    );

    @Operation(
            summary = "댓글 삭제",
            description = "댓글을 삭제합니다."
    )
    @DeleteMapping("/{commentId}")
    ResponseEntity<ApiResponse<Long>> delete(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal com.workhub.global.security.CustomUserDetails userDetails
    );
}
