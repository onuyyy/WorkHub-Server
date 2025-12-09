package com.workhub.project.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.project.dto.CreateProjectRequest;
import com.workhub.project.dto.ProjectListResponse;
import com.workhub.project.dto.ProjectResponse;
import com.workhub.project.dto.UpdateStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프로젝트", description = "프로젝트 관리 API")
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
            @RequestBody CreateProjectRequest projectRequest
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
            @RequestBody UpdateStatusRequest request
    );

    @Operation(
            summary = "프로젝트 정보 수정",
            description = "기존 프로젝트의 정보를 수정합니다. 변경된 필드만 감지하여 각 필드별로 변경 이력을 저장합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로젝트 수정 성공",
                    content = @Content(schema = @Schema(implementation = ProjectResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (필수 항목 누락 또는 유효하지 않은 데이터)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 오류 (Role Admin만 프로젝트 수정 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (프로젝트 수정 실패)"
            )
    })
    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @Parameter(description = "프로젝트 ID", required = true)
            @PathVariable("projectId") Long projectId,

            @Parameter(description = "프로젝트 수정 요청 정보", required = true)
            @RequestBody CreateProjectRequest request
    );

    @Operation(
            summary = "프로젝트 삭제",
            description = "프로젝트를 삭제합니다. 실제 삭제가 아닌 소프트 삭제(soft delete)로 처리되며, 상태가 DELETED로 변경됩니다. 삭제 이력도 함께 저장됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로젝트 삭제 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 오류 (Role Admin만 프로젝트 삭제 가능)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "프로젝트를 찾을 수 없음"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (프로젝트 삭제 실패)"
            )
    })
    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<ApiResponse<String>> deleteProject(
            @Parameter(description = "삭제할 프로젝트 ID", required = true)
            @PathVariable("projectId") Long projectId
    );

    @Operation(
            summary = "프로젝트 목록 조회",
            description = """
                    사용자의 권한(Role)에 따라 프로젝트 목록을 조회합니다.

                    **조회 권한:**
                    - CLIENT: 자신이 클라이언트 멤버로 등록된 프로젝트만 조회
                    - DEVELOPER: 자신이 개발자로 배정된 프로젝트만 조회
                    - ADMIN: 시스템의 모든 프로젝트 조회

                    **성능 최적화:**
                    - QueryDSL 배치 조회로 N+1 문제 해결 (181개 쿼리 → 6개 쿼리)
                    - 프로젝트별 클라이언트 멤버, 개발자 멤버, 워크플로우 단계, 총 인원 정보 포함
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로젝트 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ProjectListResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 오류 (로그인 필요)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 오류 (프로젝트 목록 조회 실패)"
            )
    })
    @GetMapping("/list")
    ResponseEntity<ApiResponse<List<ProjectListResponse>>> projectList();
}