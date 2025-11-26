package com.workhub.post.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.request.PostUpdateRequest;
import com.workhub.post.record.response.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "게시물 관리", description = "프로젝트 단계별 게시물 CRUD API")
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/posts")
public interface PostApi {

    @Operation(
            summary = "게시물 작성",
            description = "프로젝트 단계에서 새 게시물을 작성합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "프로젝트 단계 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "게시물 작성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 값 누락 또는 형식 오류)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (게시물 저장 실패)")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PostResponse> createPost(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody PostRequest request
    );

    @Operation(
            summary = "게시물 목록 조회",
            description = "특정 프로젝트 단계의 모든 게시물을 조회합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "프로젝트 단계 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "게시물 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = PostResponse.class)))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시물이 존재하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (게시물 조회 실패)")
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<PostResponse>> getPosts(
            @PathVariable Long projectId,
            @PathVariable Long nodeId
    );

    @Operation(
            summary = "게시물 단건 조회",
            description = "게시물 식별자로 단건 게시물을 조회합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "프로젝트 단계 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "postId", description = "게시물 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "게시물 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (게시물 조회 실패)")
    })
    @GetMapping(value = "/{postId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PostResponse> getPost(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId
    );

    // PATCH 요청으로 제목/내용/분류/해시태그/IP를 수정
    @Operation(
            summary = "게시물 수정",
            description = "게시물 식별자로 내용을 수정합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "프로젝트 단계 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "postId", description = "게시물 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "게시물 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PostResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "게시물을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (게시물 수정 실패)")
    })
    @PatchMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<PostResponse> updatePost(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request
    );
}
