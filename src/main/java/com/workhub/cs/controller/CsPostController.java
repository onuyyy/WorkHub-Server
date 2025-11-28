package com.workhub.cs.controller;

import com.workhub.cs.api.CsPostApi;
import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.dto.CsPostUpdateRequest;
import com.workhub.cs.service.CsPostService;
import com.workhub.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/csPosts")
public class CsPostController implements CsPostApi {

    private final CsPostService csPostService;

    /**
     * 프로젝트의 CS 게시글을 작성한다.
     * @param projectId 프로젝트 식별자
     * @param csPostRequest 게시글 생성 요청
     * @return CsPostResponse
     */
    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CsPostResponse> createCsPost(
            @PathVariable Long projectId,
            @Valid @RequestBody CsPostRequest csPostRequest
    ) {

        // todo : security 기능 구현시 userId security에서 꺼내서 넘겨야 함
        CsPostResponse response = csPostService.create(projectId, csPostRequest);

        return ApiResponse.created(response, "CS 게시글이 작성되었습니다.");
    }

    /**
     * 프로젝트의 CS 게시글을 수정한다.
     * @param projectId
     * @param csPostUpdateRequest
     * @return
     */
    @Override
    @PatchMapping("/{csPostId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<CsPostResponse> updateCsPost(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @Valid @RequestBody CsPostUpdateRequest csPostUpdateRequest
    ) {
        // todo : security 기능 구현시 userId security에서 꺼내서 넘겨야 함
        CsPostResponse response = csPostService.update(projectId, csPostId, csPostUpdateRequest);

        return ApiResponse.success(response, "CS 게시글이 수정되었습니다.");
    }
}
