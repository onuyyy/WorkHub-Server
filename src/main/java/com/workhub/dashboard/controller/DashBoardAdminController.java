package com.workhub.dashboard.controller;

import com.workhub.dashboard.api.DashBoardAdminApi;
import com.workhub.dashboard.dto.admin.CompanyCountResponse;
import com.workhub.dashboard.dto.admin.MonthlyMetricsResponse;
import com.workhub.dashboard.dto.admin.ProjectCountResponse;
import com.workhub.dashboard.dto.admin.UserCountResponse;
import com.workhub.dashboard.service.admin.DashBoardAdminService;
import com.workhub.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/dashboard")
public class DashBoardAdminController implements DashBoardAdminApi {

    private final DashBoardAdminService dashBoardAdminService;

    @Override
    @GetMapping("/users/count")
    public ResponseEntity<ApiResponse<UserCountResponse>> getUserCount() {

        UserCountResponse userCount = dashBoardAdminService.getUserCount();

        return ApiResponse.success(userCount, "총 유저가 조회되었습니다.");
    }

    @Override
    @GetMapping("/companies/count")
    public ResponseEntity<ApiResponse<CompanyCountResponse>> getCompanyCount() {

        CompanyCountResponse companyCount = dashBoardAdminService.getCompanyCount();

        return ApiResponse.success(companyCount, "총 회사가 조회되었습니다.");
    }

    @GetMapping("/projects/count")
    public ResponseEntity<ApiResponse<ProjectCountResponse>> getProjectCount() {

        ProjectCountResponse projectCount = dashBoardAdminService.getProjectCount();

        return ApiResponse.success(projectCount, "총 프로젝트가 조회되었습니다.");
    }

    @Override
    @GetMapping("/monthly-metrics")
    public ResponseEntity<ApiResponse<MonthlyMetricsResponse>> getMonthlyMetrics(
            @RequestParam(name = "months", required = false, defaultValue = "12") Integer months) {

        MonthlyMetricsResponse monthlyMetrics = dashBoardAdminService.getMonthlyMetrics(months);

        return ApiResponse.success(monthlyMetrics, "월별 지표가 조회되었습니다.");
    }
}
