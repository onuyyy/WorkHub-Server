package com.workhub.cs.api;

import com.workhub.cs.dto.csPost.CsPostRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.dto.csPost.CsPostSearchRequest;
import com.workhub.cs.dto.csPost.CsPostUpdateRequest;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.security.CustomUserDetails;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CS 게시글 관리", description = "프로젝트 CS 게시물 API")
@RequestMapping("/api/v1/projects/{projectId}/csPosts")
public interface CsPostApi {

    @Operation(
            summary = "CS 게시글 단건 조회",
            description = "프로젝트와 게시글 식별자를 사용해 단일 CS 게시글을 조회합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 게시글 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsPostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CS 게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/{csPostId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CsPostResponse>> findCsPost(
            @PathVariable Long projectId,
            @PathVariable Long csPostId
    );

    @Operation(
            summary = "CS 게시글 목록 조회",
            description = "프로젝트의 모든 CS 게시글을 검색 조건과 페이징 정보로 조회합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "searchValue", description = "제목/내용 검색 키워드", in = ParameterIn.QUERY),
                    @Parameter(name = "csPostStatus", description = "CS 게시글 상태 필터", in = ParameterIn.QUERY)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 게시글 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsPostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Page<CsPostResponse>>> findCsPosts(
            @PathVariable Long projectId,
            @ParameterObject CsPostSearchRequest csPostSearchRequest,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(
            summary = "CS 게시글 작성",
            description = "프로젝트에 속한 CS 게시글을 생성합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "CS 게시글 작성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsPostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 값 누락 또는 형식 오류)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (CS 게시글 저장 실패)")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CsPostResponse>> createCsPost(
            @PathVariable Long projectId,
            @Valid @RequestBody CsPostRequest csPostRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "CS 게시글 수정",
            description = "CS 게시글 식별자로 제목, 내용 및 첨부 파일을 수정합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 게시글 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsPostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CS 게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (CS 게시글 수정 실패)")
    })
    @PatchMapping(value = "/{csPostId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CsPostResponse>> updateCsPost(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @Valid @RequestBody CsPostUpdateRequest csPostUpdateRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "CS 게시글 삭제",
            description = "CS 게시글을 Soft Delete 방식으로 삭제합니다. (deletedAt 설정)",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 게시글 삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Long.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CS 게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 부족(추후 Security 적용 시)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping(
            value = "/{csPostId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<ApiResponse<Long>> deleteCsPost(
            @PathVariable Long projectId,
            @PathVariable Long csPostId
    );

    @Operation(
            summary = "CS 게시글 상태 변경",
            description = "CS 게시글의 진행 상태(RECEIVED, IN_PROGRESS, COMPLETED)를 변경합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "status", description = "변경할 CS 게시글 상태 값", in = ParameterIn.QUERY, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 게시글 상태 변경 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsPostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "CS 게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 부족"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/{csPostId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CsPostStatus>> changeStatus(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @RequestParam CsPostStatus status
    );
}
