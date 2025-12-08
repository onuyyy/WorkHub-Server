package com.workhub.projectNode.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.projectNode.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프로젝트 노드", description = "프로젝트 노드 관리 API")
public interface ProjectNodeApi {

    @Operation(
            summary = "프로젝트 노드 리스트 조회",
            description = "프로젝트에 속한 모든 노드의 리스트를 조회합니다. 노드는 순서(nodeOrder)를 기준으로 정렬되어 반환됩니다. " +
                    "각 노드는 제목, 설명, 상태, 우선순위, 순서, 계약 기간 등의 정보를 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "노드 리스트 조회 성공",
                    content = @Content(schema = @Schema(implementation = NodeListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류"
            )
    })
    @GetMapping
    ResponseEntity<ApiResponse<List<NodeListResponse>>> getNodeList(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable("projectId") Long projectId
    );

    @Operation(
            summary = "프로젝트 노드 생성",
            description = "프로젝트 내에 새로운 노드를 생성합니다. 노드는 프로젝트의 작업 단위를 나타내며, 제목, 설명, 우선순위, 순서 정보를 포함합니다. " +
                    "같은 순서의 노드가 이미 존재하는 경우, 기존 노드들의 순서가 자동으로 조정됩니다. 생성 이력도 함께 저장됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "프로젝트 노드 생성 성공",
                    content = @Content(schema = @Schema(implementation = CreateNodeResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 항목 누락, 유효하지 않은 우선순위 값 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (노드 생성 실패, 히스토리 저장 실패 등)"
            )
    })
    @PostMapping
    ResponseEntity<ApiResponse<CreateNodeResponse>> createNode(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable Long projectId,

            @Parameter(description = "노드 생성 요청 정보 (제목, 설명, 순서, 우선순위)", required = true)
            @RequestBody CreateNodeRequest request
    );

    @Operation(
            summary = "프로젝트 노드 상태 변경",
            description = "프로젝트 노드의 상태를 변경합니다. 상태 변경 이력도 함께 저장됩니다. " +
                    "가능한 상태: NOT_STARTED(시작 전), IN_PROGRESS(진행 중), PENDING_REVIEW(검토 대기), ON_HOLD(보류)"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "노드 상태 변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 상태 값 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "노드를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (상태 변경 실패, 히스토리 저장 실패 등)"
            )
    })
    @PatchMapping("{nodeId}/status")
    ResponseEntity<ApiResponse<String>> updateNodeStatus(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable("projectId") Long projectId,

            @Parameter(description = "노드 ID", required = true)
            @PathVariable("nodeId") Long nodeId,

            @Parameter(description = "변경할 노드 상태 정보", required = true)
            @RequestBody UpdateNodeStatusRequest request
    );

    @Operation(
            summary = "프로젝트 노드 순서 변경",
            description = "프로젝트에 속한 여러 노드들의 순서를 일괄적으로 변경합니다. " +
                    "각 노드의 ID와 새로운 순서 값을 포함한 리스트를 받아, 해당 노드들의 순서를 업데이트합니다. " +
                    "변경이 없는 노드(기존 순서와 동일한 경우)는 업데이트를 건너뛰며, 변경된 노드에 대해서만 히스토리를 기록합니다. " +
                    "요청된 노드 ID가 해당 프로젝트에 속하지 않는 경우 예외가 발생합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "노드 순서 변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 항목 누락, 유효하지 않은 순서 값 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 노드를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (순서 변경 실패, 히스토리 저장 실패 등)"
            )
    })
    @PatchMapping("/order")
    ResponseEntity<ApiResponse<String>> updateNodeOrder(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable("projectId") Long projectId,

            @Parameter(description = "노드 순서 변경 요청 리스트 (각 노드의 ID와 새로운 순서)", required = true)
            @RequestBody List<UpdateNodOrderRequest> request
    );

    @Operation(
            summary = "프로젝트 노드 정보 수정",
            description = "프로젝트 노드의 내용을 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "노드 정보 변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 항목 누락, 유효하지 않은 순서 값 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "프로젝트 또는 노드를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (정보 변경 실패, 히스토리 저장 실패 등)"
            )
    })
    @PutMapping("{nodeId}")
    public ResponseEntity<ApiResponse<CreateNodeResponse>> updateNode(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable Long projectId,

            @Parameter(description = "프로젝트 노드 ID", required = true)
            @PathVariable Long nodeId,

            @Parameter(description = "프로젝트 노드 수정 요청 정보", required = true)
            @RequestBody UpdateNodeRequest request
    );
}