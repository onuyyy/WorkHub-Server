package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.CompanyApi;
import com.workhub.userTable.dto.CompanyRegisterRequest;
import com.workhub.userTable.dto.CompanyResponse;
import com.workhub.userTable.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class CompanyController implements CompanyApi {

    private final CompanyService companyService;

    @PostMapping("/users/add/company")
    public ResponseEntity<ApiResponse<CompanyResponse>> registerCompany(@RequestBody @Valid CompanyRegisterRequest request) {
        CompanyResponse response = companyService.registerCompany(request);
        return ApiResponse.created(response, "고객사가 등록되었습니다.");
    }
}
