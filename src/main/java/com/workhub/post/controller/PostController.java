package com.workhub.post.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.post.record.request.PostCreateRequest;
import com.workhub.post.record.response.PostResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/posts")
public class PostController {
    private final PostService postService;

    /**
     * 프로젝트/노드별 게시글을 생성한다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId 프로젝트 단계 식별자
     * @param request 게시글 생성 요청
     * @return ApiResponse 래퍼로 감싼 생성 결과
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody PostCreateRequest request) {
        Post created = postService.create(request);
        return ApiResponse.created(PostResponse.from(created), "게시글이 생성되었습니다.");
    }
}
