package com.workhub.cs.controller;

import com.workhub.cs.api.CsQnaApi;
import com.workhub.cs.dto.csQna.CsQnaRequest;
import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.service.csQna.CreateCsQnaService;
import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/csPosts/{csPostId}")
public class CsQnaController implements CsQnaApi {

    private final CreateCsQnaService createCsQnaService;

    /**
     * CS POST의 댓글을 작성한다.
     *
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param csQnaRequest 요청 DTO
     * @param userDetails 유저 정보
     * @return CsQnsResponse
     */
    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<CsQnaResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @RequestBody @Valid CsQnaRequest csQnaRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CsQnaResponse response = createCsQnaService.create(
                projectId, csPostId, userDetails.getUserId(), csQnaRequest);

        return ApiResponse.success(response, "CS Comment가 작성되었습니다.");
    }
}
