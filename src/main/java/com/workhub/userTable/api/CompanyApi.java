package com.workhub.userTable.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.company.request.CompanyRegisterRequest;
import com.workhub.userTable.dto.company.request.CompanyStatusUpdateRequest;
import com.workhub.userTable.dto.company.response.CompanyDetailResponse;
import com.workhub.userTable.dto.company.response.CompanyListResponse;
import com.workhub.userTable.dto.company.response.CompanyResponse;
import com.workhub.userTable.dto.company.response.CompanyTitleResponse;
import com.workhub.userTable.dto.user.response.UserNameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "고객사 관리", description = "고객사 등록 API")
public interface CompanyApi {

    @Operation(
            summary = "고객사 등록",
            description = "관리자가 신규 고객사를 등록합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "고객사 등록 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompanyResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 고객사"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (고객사 등록 실패)")
    })
    ResponseEntity<ApiResponse<CompanyResponse>> registerCompany(@RequestBody @Valid CompanyRegisterRequest request);

    @Operation(
            summary = "고객사 목록 조회",
            description = "등록된 고객사 전체 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "고객사 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompanyListResponse.class))
            )
    })
    ResponseEntity<ApiResponse<Page<CompanyListResponse>>> getCompanies(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(
            summary = "고객사 상호 목록 조회",
            description = "등록된 고객사 전체 상호 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "고객사 상호 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompanyTitleResponse.class))
            )
    })
    public ResponseEntity<ApiResponse<List<CompanyTitleResponse>>> getCompanyNames();

    @Operation(
            summary = "고객사 상세 조회",
            description = "고객사 ID를 기준으로 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "고객사 상세 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompanyDetailResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 고객사"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResponse<CompanyDetailResponse>> getCompany(@PathVariable("companyId") Long companyId);

    @Operation(
            summary = "고객사 삭제",
            description = "고객사 ID를 기준으로 고객사를 비활성화합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "고객사 삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 고객사")
    })
    ResponseEntity<ApiResponse<Object>> deleteCompany(@PathVariable("companyId") Long companyId);

    @Operation(
            summary = "고객사 상태 변경",
            description = "고객사 ID와 상태 값을 받아 상태를 변경합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "고객사 상태 변경 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CompanyResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 고객사"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResponse<CompanyResponse>> updateCompanyStatus(
            @PathVariable("companyId") Long companyId,
            @RequestBody @Valid CompanyStatusUpdateRequest request
    );

    @Operation(
            summary = "고객사 소속 직원 목록 조회",
            description = "고객사 ID를 기준으로 해당 고객사에 소속된 직원 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "직원 목록 조회 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserNameResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 고객사"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ApiResponse<List<UserNameResponse>>> getMemberList(@PathVariable("companyId") Long companyId);
}
