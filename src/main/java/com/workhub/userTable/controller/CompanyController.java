package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.CompanyApi;
import com.workhub.userTable.dto.company.request.CompanyRegisterRequest;
import com.workhub.userTable.dto.company.request.CompanyStatusUpdateRequest;
import com.workhub.userTable.dto.company.response.CompanyDetailResponse;
import com.workhub.userTable.dto.company.response.CompanyListResponse;
import com.workhub.userTable.dto.company.response.CompanyResponse;
import com.workhub.userTable.dto.company.response.CompanyTitleResponse;
import com.workhub.userTable.dto.user.response.UserNameResponse;
import com.workhub.userTable.service.CompanyService;
import com.workhub.userTable.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/company")
@RequiredArgsConstructor
public class CompanyController implements CompanyApi {

    private final CompanyService companyService;
    private final UserService userService;

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CompanyResponse>> registerCompany(@RequestBody @Valid CompanyRegisterRequest request) {
        CompanyResponse response = companyService.registerCompany(request);
        return ApiResponse.created(response, "고객사가 등록되었습니다.");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyListResponse>>> getCompanys() {
        List<CompanyListResponse> companys = companyService.getCompanys();
        return ApiResponse.success(companys);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<CompanyTitleResponse>>> getCompanyNames() {

        List<CompanyTitleResponse> response = companyService.getCompanyNameList();
        return ApiResponse.success(response);
    }

    @GetMapping("/detail/{companyId}")
    public ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompany(@PathVariable("companyId") Long companyId) {

        CompanyDetailResponse company = companyService.getCompany(companyId);
        return ApiResponse.success(company);
    }
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Object>> deleteCompany(@PathVariable("companyId") Long companyId) {
            companyService.deleteCompany(companyId);
            return ApiResponse.success(null, "고객사가 비활성화되었습니다.");
    }

    @PatchMapping("/{companyId}/status")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompanyStatus(
            @PathVariable("companyId") Long companyId,
            @RequestBody @Valid CompanyStatusUpdateRequest request
    ) {
        CompanyResponse response = companyService.updateCompanyStatus(companyId, request.status());
        return ApiResponse.success(response, "고객사 상태가 변경되었습니다.");
    }

    @GetMapping("/{companyId}/list")
    public ResponseEntity<ApiResponse<List<UserNameResponse>>> getMemberList(@PathVariable("companyId") Long companyId) {

        List<UserNameResponse> responses = userService.getUserMapByCompanyIdIn(companyId);
        return ApiResponse.success(responses);
    }
}
