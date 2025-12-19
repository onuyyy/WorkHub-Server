package com.workhub.checklist.api;

import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.dto.comment.CheckListCommentUpdateRequest;
import com.workhub.global.response.ApiResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PatchMapping;

@Tag(name = "체크리스트 댓글", description = "체크리스트 항목 댓글 API")
@RequestMapping("/api/v1/projects/{projectId}/nodes/{nodeId}/checkLists")
public interface CheckListCommentApi {

    @Operation(
            summary = "체크리스트 댓글 작성",
            description = "특정 체크리스트 항목에 댓글을 작성합니다.",
            parameters = {
                    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "프로젝트 식별자", required = true),
                    @Parameter(name = "nodeId", in = ParameterIn.PATH, description = "노드 식별자", required = true),
                    @Parameter(name = "checkListId", in = ParameterIn.PATH, description = "체크리스트 식별자", required = true),
                    @Parameter(name = "checkListItemId", in = ParameterIn.PATH, description = "체크리스트 항목 식별자", required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "댓글 작성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListCommentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "체크리스트 혹은 항목을 찾을 수 없음")
    })
    @PostMapping(value = "/{checkListId}/items/{checkListItemId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CheckListCommentResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @Valid @org.springframework.web.bind.annotation.RequestPart("data") CheckListCommentRequest request,
            @org.springframework.web.bind.annotation.RequestPart(value = "files", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> files
    );

    @Operation(
            summary = "체크리스트 댓글 수정",
            description = "특정 체크리스트 항목의 댓글을 수정합니다.",
            parameters = {
                    @Parameter(name = "projectId", in = ParameterIn.PATH, description = "프로젝트 식별자", required = true),
                    @Parameter(name = "nodeId", in = ParameterIn.PATH, description = "노드 식별자", required = true),
                    @Parameter(name = "checkListId", in = ParameterIn.PATH, description = "체크리스트 식별자", required = true),
                    @Parameter(name = "checkListItemId", in = ParameterIn.PATH, description = "체크리스트 항목 식별자", required = true),
                    @Parameter(name = "commentId", in = ParameterIn.PATH, description = "댓글 식별자", required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "댓글 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListCommentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "체크리스트 혹은 댓글을 찾을 수 없음")
    })
    @PatchMapping(value = "/{checkListId}/items/{checkListItemId}/comments/{commentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CheckListCommentResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @PathVariable Long commentId,
            @Valid @org.springframework.web.bind.annotation.RequestPart("data") CheckListCommentUpdateRequest request,
            @org.springframework.web.bind.annotation.RequestPart(value = "newFiles", required = false) java.util.List<org.springframework.web.multipart.MultipartFile> newFiles
    );
}
