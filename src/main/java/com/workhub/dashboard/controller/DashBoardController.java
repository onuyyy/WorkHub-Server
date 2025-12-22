package com.workhub.dashboard.controller;

import com.workhub.dashboard.api.DashBoardApi;
import com.workhub.dashboard.dto.DashBoardResponse;
import com.workhub.dashboard.service.DashBoardService;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dashboard")
public class DashBoardController implements DashBoardApi {

    private final DashBoardService dashBoardService;

    @Override
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashBoardResponse>> getSummary() {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        DashBoardResponse response = dashBoardService.getSummary(userId);
        return ApiResponse.success(response);
    }
}
