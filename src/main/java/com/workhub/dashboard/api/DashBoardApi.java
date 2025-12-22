package com.workhub.dashboard.api;

import com.workhub.dashboard.dto.DashBoardResponse;
import com.workhub.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "대시보드", description = "개발사/고객사 별 요약 정보를 제공합니다.")
@RequestMapping("/api/v1/dashboard")
public interface DashBoardApi {

    /**
     * 대시보드 요약 조회
     * - 로그인한 사용자의 소속 프로젝트 기준으로 승인 대기 건수와 총 프로젝트 개수를 반환합니다.
     * - 응답 필드:
     *   pendingApprovals: 소속 프로젝트의 승인 대기(PENDING_REVIEW) 노드 건수
     *   totalProjects: 사용자가 소속된 프로젝트 수
     */
    @Operation(summary = "대시보드 요약 조회", description = "로그인한 사용자의 소속 프로젝트 기준 요약 정보를 반환합니다.")
    @GetMapping("/summary")
    ResponseEntity<ApiResponse<DashBoardResponse>> getSummary();
}
