package com.workhub.userTable.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.email.EmailVerificationConfirmRequest;
import com.workhub.userTable.dto.email.EmailVerificationSendRequest;
import com.workhub.userTable.dto.email.EmailVerificationStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "이메일 인증", description = "이메일 인증 코드 발송 및 검증 API")
public interface EmailVerificationApi {

    @Operation(
            summary = "이메일 인증 코드 발송",
            description = "입력한 이메일 주소로 인증 코드를 발송합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "발송 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<Void>> send(@RequestBody @Valid EmailVerificationSendRequest request);

    @Operation(
            summary = "이메일 인증 코드 확인",
            description = "발송받은 인증 코드를 검증합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "검증 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "검증 실패")
    })
    ResponseEntity<ApiResponse<EmailVerificationStatusResponse>> confirm(@RequestBody @Valid EmailVerificationConfirmRequest request);

    @Operation(
            summary = "이메일 인증 여부 조회",
            description = "해당 이메일이 인증되었는지 확인합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<EmailVerificationStatusResponse>> status(@RequestParam("email") String email);
}
