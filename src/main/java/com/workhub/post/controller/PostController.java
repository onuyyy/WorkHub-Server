package com.workhub.post.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.post.api.PostApi;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.response.PostResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/posts")
public class PostController implements PostApi {
    private final PostService postService;

    /**
     * 프로젝트/노드별 게시글을 생성한다.
     *
     * @param projectId 프로젝트 식별자
     * @param nodeId 프로젝트 단계 식별자
     * @param request 게시글 생성 요청
     * @return ApiResponse 래퍼로 감싼 생성 결과
     */
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<PostResponse> createPost(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody PostRequest request) {
        Post created = postService.create(request);
        return ApiResponse.created(PostResponse.from(created), "게시글이 생성되었습니다.");
    }
    /**
     * 프로젝트/노드별 게시글 목록을 조회한다.
     *
     * @param projectId 프로젝트 식별자 (추후 검증 로직에 사용 예정)
     * @param nodeId    프로젝트 단계 식별자 (추후 검증 로직에 사용 예정)
     * @return ApiResponse<List<PostResponse>>
     */
    @Override
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<PostResponse>> getPosts(@PathVariable Long projectId, @PathVariable Long nodeId) {
        List<PostResponse> responses = postService.findAll()
                .stream()
                .map(PostResponse::from)
                .toList();
        return ApiResponse.success(responses, "게시글 목록 조회에 성공했습니다.");
    }

    @Override
    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long projectId,
                                             @PathVariable Long nodeId,
                                             @PathVariable Long postId) {
        PostResponse response = PostResponse.from(postService.findById(postId));
        return ApiResponse.success(response, "게시글 조회에 성공했습니다.");
    }
}
