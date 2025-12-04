package com.workhub.cs.controller;

import com.workhub.cs.api.CsQnaApi;
import com.workhub.cs.dto.csQna.CsQnaRequest;
import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.dto.csQna.CsQnaUpdateRequest;
import com.workhub.cs.service.csQna.CreateCsQnaService;
import com.workhub.cs.service.csQna.DeleteCsQnaService;
import com.workhub.cs.service.csQna.ReadCsQnaService;
import com.workhub.cs.service.csQna.UpdateCsQnaService;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/csPosts/{csPostId}/qnas")
public class CsQnaController implements CsQnaApi {

    private final CreateCsQnaService createCsQnaService;
    private final ReadCsQnaService readCsQnaService;
    private final UpdateCsQnaService updateCsQnaService;
    private final DeleteCsQnaService deleteCsQnaService;

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

    /**
     * CS POST의 댓글 목록을 조회한다.
     *
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param pageable 페이징 정보 (최상위 댓글 기준)
     * @return Page<CsQnaResponse> 계층 구조로 구성된 댓글 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CsQnaResponse>>> findCsQnas(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CsQnaResponse> response = readCsQnaService.findCsQnas(
                projectId, csPostId, pageable);

        return ApiResponse.success(response, "CS Comment 목록이 조회되었습니다.");
    }

    /**
     * CS POST의 댓글을 수정한다.
     *
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param csQnaId 댓글 식별자
     * @param csQnaUpdateRequest 댓글 수정 요청
     * @param userDetails 유저 식별자
     * @return CsQnaResponse
     */
    @Override
    @PatchMapping("/{csQnaId}")
    public ResponseEntity<ApiResponse<CsQnaResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @PathVariable Long csQnaId,
            @RequestBody @Valid CsQnaUpdateRequest csQnaUpdateRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CsQnaResponse response = updateCsQnaService.update(
                projectId, csPostId, csQnaId, userDetails.getUserId(), csQnaUpdateRequest);

        return ApiResponse.success(response, "CS Comment가 수정되었습니다.");
    }

    /**
     * CS POST의 댓글을 삭제한다. 댓글 삭제 시 모든 자식 댓글도 함께 삭제된다.
     *
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param csQnaId 댓글 식별자
     * @param userDetails 유저 정보
     * @return 삭제된 댓글 id
     */
    @Override
    @DeleteMapping("/{csQnaId}")
    public ResponseEntity<ApiResponse<Long>> delete(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @PathVariable Long csQnaId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long deletedId = deleteCsQnaService.delete(
                projectId, csPostId, csQnaId, userDetails.getUserId());

        return ApiResponse.success(deletedId, "CS Comment가 삭제되었습니다.");
    }
}
