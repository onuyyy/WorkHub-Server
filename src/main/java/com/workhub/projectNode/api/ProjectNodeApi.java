package com.workhub.projectNode.api;

import com.workhub.global.clientInfo.ClientInfo;
import com.workhub.global.clientInfo.ClientInfoDto;
import com.workhub.global.response.ApiResponse;
import com.workhub.projectNode.dto.CreateNodeRequest;
import com.workhub.projectNode.dto.CreateNodeResponse;
import com.workhub.userTable.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Project Node", description = "프로젝트 노드 관리 API")
public interface ProjectNodeApi {

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
            @RequestBody CreateNodeRequest request,

            @Parameter(hidden = true)
            @ClientInfo ClientInfoDto clientInfoDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}