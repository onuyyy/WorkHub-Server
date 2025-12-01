package com.workhub.project.api;

import com.workhub.global.clientInfo.ClientInfo;
import com.workhub.global.clientInfo.ClientInfoDto;
import com.workhub.global.response.ApiResponse;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.dto.UpdateStatusRequest;
import com.workhub.userTable.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Project", description = "프로젝트 관리 API")
public interface ProjectApi {

    @Operation(
            summary = "프로젝트 생성",
            description = "새로운 프로젝트를 생성합니다. 프로젝트 정보, 고객사 담당자, 개발사 담당자 정보를 포함하여 등록하며, 생성 이력도 함께 저장됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "프로젝트 생성 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 항목 누락 또는 유효하지 않은 데이터)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 오류 (Role Admin만 프로젝트 생성 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (프로젝트 생성 실패)"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Parameter(description = "프로젝트 생성 요청 정보", required = true)
            @RequestBody CreateProjectRequest projectRequest,

            @Parameter(hidden = true)
            @ClientInfo ClientInfoDto clientInfoDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "프로젝트 상태 변경",
            description = "기존 프로젝트의 상태를 변경합니다. 상태 변경 이력도 함께 저장됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "상태 변경 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효하지 않은 상태값)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 오류 (Role Admin만 상태 변경 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (상태 변경 실패)"
            )
    })
    @PatchMapping("/{projectId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<String>> updateStatus(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable("projectId") Long projectId,

            @Parameter(description = "변경할 상태 정보", required = true)
            @RequestBody UpdateStatusRequest request,

            @Parameter(hidden = true)
            @ClientInfo ClientInfoDto clientInfoDto,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails userDetails
    );
}