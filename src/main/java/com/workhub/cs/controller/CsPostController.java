package com.workhub.cs.controller;

import com.workhub.cs.api.CsPostApi;
import com.workhub.cs.dto.csPost.CsPostRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.dto.csPost.CsPostSearchRequest;
import com.workhub.cs.dto.csPost.CsPostUpdateRequest;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.cs.service.csPost.CreateCsPostService;
import com.workhub.cs.service.csPost.DeleteCsPostService;
import com.workhub.cs.service.csPost.ReadCsPostService;
import com.workhub.cs.service.csPost.UpdateCsPostService;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/projects/{projectId}/csPosts")
public class CsPostController implements CsPostApi {

    private final CreateCsPostService createCsPostService;
    private final UpdateCsPostService updateCsPostService;
    private final ReadCsPostService readCsPostService;
    private final DeleteCsPostService deleteCsPostService;

    /**
     * 프로젝트의 CS 게시글을 단건 조회한다.
     *
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @return CsPostResponse
     */
    @Override
    @GetMapping("/{csPostId}")
    public ResponseEntity<ApiResponse<CsPostResponse>> findCsPost(
            @PathVariable Long projectId,
            @PathVariable Long csPostId
    ) {
        CsPostResponse response = readCsPostService.findCsPost(projectId, csPostId);

        return ApiResponse.success(response, "CS 게시글이 조회되었습니다.");
    }

    /**
     * 프로젝트의 CS 게시글 리스트를 조회한다.
     *
     * @param projectId 프로젝트 식별자
     * @param pageable 페이징 정보
     * @return CsPostResponse
     */
    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CsPostResponse>>> findCsPosts(
            @PathVariable Long projectId,
            CsPostSearchRequest csPostSearchRequest,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CsPostResponse> csPosts = readCsPostService.findCsPosts(projectId, csPostSearchRequest, pageable);

        return ApiResponse.success(csPosts ,"CS 게시글 목록이 조회되었습니다.");
    }

    /**
     * 프로젝트의 CS 게시글을 작성한다.
     *
     * @param projectId     프로젝트 식별자
     * @param csPostRequest 게시글 생성 요청
     * @param files         첨부 파일 목록
     * @param userDetails   인증된 사용자 정보
     * @return CsPostResponse
     */
    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CsPostResponse>> createCsPost(
            @PathVariable Long projectId,
            @Valid @RequestPart("data") CsPostRequest csPostRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CsPostResponse response = createCsPostService.create(projectId, userDetails.getUserId(), csPostRequest, files);

        return ApiResponse.created(response, "CS 게시글이 작성되었습니다.");
    }

    /**
     * 프로젝트의 CS 게시글을 수정한다.
     *
     * @param projectId           프로젝트 식별자
     * @param csPostId            게시글 식별자
     * @param csPostUpdateRequest 게시글 수정 요청
     * @param newFiles            새로 추가할 파일 목록
     * @param userDetails         인증된 사용자 정보
     * @return CsPostResponse
     */
    @Override
    @PatchMapping(value = "/{csPostId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CsPostResponse>> updateCsPost(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @Valid @RequestPart("data") CsPostUpdateRequest csPostUpdateRequest,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CsPostResponse response = updateCsPostService.update(projectId, csPostId, userDetails.getUserId(), csPostUpdateRequest, newFiles);

        return ApiResponse.success(response, "CS 게시글이 수정되었습니다.");
    }

    /**
     * 프로젝트의 CS 게시물을 삭제한다.
     *
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @return 삭제된 게시글 ID
     */
    @Override
    @DeleteMapping("/{csPostId}")
    public ResponseEntity<ApiResponse<Long>> deleteCsPost(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long deletedId = deleteCsPostService.delete(projectId, csPostId, userDetails.getUserId());
        return ApiResponse.success(deletedId);
    }

    /**
     * 프로젝트 CS POST 상태 값을 변경한다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param status 게시글 상태 값
     * @return CsPostStatus 변경된 상태 값
     */
    @Override
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN')")
    @PatchMapping("/{csPostId}/status")
    public ResponseEntity<ApiResponse<CsPostStatus>> changeStatus(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @RequestParam CsPostStatus status
    ) {
        CsPostStatus changed = updateCsPostService.changeStatus(projectId, csPostId, status);

        return ApiResponse.success(changed, "CS 게시글 상태가 변경되었습니다.");
    }
}
