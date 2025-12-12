package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.CompanyApi;
import com.workhub.userTable.dto.*;
import com.workhub.userTable.service.CompanyService;
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

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CompanyResponse>> registerCompany(@RequestBody @Valid CompanyRegisterRequest request) {
        CompanyResponse response = companyService.registerCompany(request);
        return ApiResponse.created(response, "고객사가 등록되었습니다.");
    }

    @GetMapping
    public List<CompanyListResponse> getCompanys() {
        return companyService.getCompanys();
    }

    @GetMapping("/detail/{companyId}")
    public CompanyDetailResponse getCompany(@PathVariable("companyId") Long companyId) {
        return companyService.getCompany(companyId);
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
}
