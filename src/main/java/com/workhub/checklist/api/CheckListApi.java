package com.workhub.checklist.api;

import com.workhub.checklist.dto.checkList.CheckListCreateRequest;
import com.workhub.checklist.dto.checkList.CheckListItemStatus;
import com.workhub.checklist.dto.checkList.CheckListResponse;
import com.workhub.checklist.dto.checkList.CheckListTemplateRequest;
import com.workhub.checklist.dto.checkList.CheckListTemplateResponse;
import com.workhub.checklist.dto.checkList.CheckListUpdateRequest;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "체크리스트 관리", description = "프로젝트 노드의 체크리스트 API")
@RequestMapping("/api/v1/projects/{projectId}")
public interface CheckListApi {

    @Operation(
            summary = "체크리스트 생성",
            description = "프로젝트 노드에 체크리스트를 생성합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "체크리스트 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트 또는 노드를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/nodes/{nodeId}/checkLists", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CheckListResponse>> create(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestPart("data") CheckListCreateRequest request,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "체크리스트 템플릿 목록 조회",
            description = "저장된 체크리스트 템플릿 목록을 조회합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "체크리스트 템플릿 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListTemplateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트 또는 노드를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/nodes/{nodeId}/checkLists/templates", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<List<CheckListTemplateResponse>>> findTemplates(
            @PathVariable Long projectId,
            @PathVariable Long nodeId
    );

    @Operation(
            summary = "체크리스트 템플릿 단건 조회",
            description = "템플릿 ID로 특정 템플릿을 조회합니다. 클라이언트에서 작성 폼에 불러올 때 사용합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "templateId", description = "템플릿 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "템플릿 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListTemplateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "템플릿을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/nodes/{nodeId}/checkLists/templates/{templateId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CheckListTemplateResponse>> findTemplateById(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long templateId
    );

    @Operation(
            summary = "체크리스트 템플릿 저장",
            description = "체크리스트 항목 구성을 템플릿으로 저장합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "체크리스트 템플릿 저장 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListTemplateResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "프로젝트 또는 노드를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping(value = "/nodes/{nodeId}/checkLists/templates", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CheckListTemplateResponse>> createTemplate(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestBody CheckListTemplateRequest request
    );

    @Operation(
            summary = "체크리스트 조회",
            description = "프로젝트 노드의 체크리스트를 조회합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "체크리스트 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "체크리스트를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping(value = "/nodes/{nodeId}/checkLists", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CheckListResponse>> findCheckList(
            @PathVariable Long projectId,
            @PathVariable Long nodeId
    );

    @Operation(
            summary = "체크리스트 수정",
            description = "프로젝트 노드의 체크리스트를 부분 수정합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "체크리스트 수정 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "체크리스트를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/nodes/{nodeId}/checkLists", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse<CheckListResponse>> update(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @Valid @RequestPart("data") CheckListUpdateRequest request,
            @RequestPart(value = "newFiles", required = false) List<MultipartFile> newFiles
    );

    @Operation(
            summary = "체크리스트 아이템 상태 변경",
            description = "클라이언트가 체크리스트 항목의 상태를 입력 혹은 수정합니다.",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "checkListId", description = "체크리스트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "checkListItemId", description = "체크리스트 항목 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "status", description = "입력할 상태값", in = ParameterIn.QUERY, required = false)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "체크리스트 항목 상태 변경 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CheckListItemStatus.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "체크리스트 또는 항목을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/nodes/{nodeId}/checkLists/{checkListId}/items/{checkListItemId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<CheckListItemStatus>> updateStatus(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @RequestParam(required = false) CheckListItemStatus status
    );

    @Operation(
            summary = "체크리스트 옵션 선택 토글",
            description = "클라이언트가 체크리스트 옵션의 선택 상태를 토글합니다. (true ↔ false)",
            parameters = {
                    @Parameter(name = "projectId", description = "프로젝트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "nodeId", description = "노드 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "checkListId", description = "체크리스트 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "checkListItemId", description = "체크리스트 항목 식별자", in = ParameterIn.PATH, required = true),
                    @Parameter(name = "optionId", description = "체크리스트 옵션 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "옵션 선택 상태 토글 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Boolean.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한이 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "체크리스트 또는 옵션을 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PatchMapping(value = "/nodes/{nodeId}/checkLists/{checkListId}/items/{checkListItemId}/options/{optionId}/toggle", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ApiResponse<Boolean>> toggleOptionSelection(
            @PathVariable Long projectId,
            @PathVariable Long nodeId,
            @PathVariable Long checkListId,
            @PathVariable Long checkListItemId,
            @PathVariable Long optionId
    );
}
