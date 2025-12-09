package com.workhub.userTable.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.CompanyRegisterRequest;
import com.workhub.userTable.dto.CompanyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

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
    ResponseEntity<ApiResponse<CompanyResponse>> registerCompany(CompanyRegisterRequest request);
}
