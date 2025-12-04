package com.workhub.cs.api;

import com.workhub.cs.dto.csQna.CsQnaRequest;
import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.dto.csQna.CsQnaUpdateRequest;
import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "CS 댓글 관리", description = "CS 게시글의 QnA API")
@RequestMapping("/api/v1/projects/{projectId}/csPosts/{csPostId}/qnas")
public interface CsQnaApi {

    @Operation(
            summary = "CS 댓글 작성",
            description = "완료된 프로젝트의 CS 게시글에 댓글을 작성합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "CS 댓글 작성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsQnaResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 빈 내용, 다른 게시글의 부모 댓글)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 댓글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CsQnaResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @Valid @RequestBody CsQnaRequest csQnaRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "CS 댓글 수정",
            description = "완료된 프로젝트의 CS 게시글에 작성된 댓글을 수정합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csQnaId", description = "CS 댓글 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 댓글 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CsQnaResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 빈 내용)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 댓글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/{csQnaId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CsQnaResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @PathVariable Long csQnaId,
            @Valid @RequestBody CsQnaUpdateRequest csQnaUpdateRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "CS 댓글 삭제",
            description = "완료된 프로젝트의 CS 게시글에 작성된 댓글을 삭제합니다. 댓글 삭제 시 모든 자식 댓글(답글)도 함께 삭제됩니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csQnaId", description = "CS 댓글 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 댓글 삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 이미 삭제된 댓글)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글 또는 댓글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @DeleteMapping(value = "/{csQnaId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Long>> delete(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @PathVariable Long csQnaId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "CS 댓글 목록 조회",
            description = "완료된 프로젝트의 CS 게시글에 작성된 댓글 목록을 계층 구조로 조회합니다. 최상위 댓글만 페이징되며, 각 댓글의 답글은 children 필드에 모두 포함됩니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "csPostId", description = "CS 게시글 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "CS 댓글 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Page<CsQnaResponse>>> findCsQnas(
            @PathVariable Long projectId,
            @PathVariable Long csPostId,
            @PageableDefault(size = 20) Pageable pageable
    );
}
