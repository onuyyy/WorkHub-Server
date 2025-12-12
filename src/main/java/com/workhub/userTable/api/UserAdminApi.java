package com.workhub.userTable.api;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.user.request.AdminPasswordResetRequest;
import com.workhub.userTable.dto.user.request.UserRegisterRecord;
import com.workhub.userTable.dto.user.request.UserRoleUpdateRequest;
import com.workhub.userTable.dto.user.response.UserTableResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "관리자 사용자 관리", description = "관리자 전용 사용자 생성/권한 관리/삭제 API")
public interface UserAdminApi {

    @Operation(
            summary = "관리자 사용자 생성",
            description = "관리자가 신규 사용자 계정을 생성합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "사용자 생성 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserTableResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 존재하는 로그인 아이디"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (사용자 생성 실패)")
    })
    ResponseEntity<ApiResponse<UserTableResponse>> register(UserRegisterRecord registerRecord);

    @Operation(
            summary = "관리자 비밀번호 초기화",
            description = "관리자가 지정한 사용자 계정의 비밀번호를 초기화합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "비밀번호를 초기화할 사용자 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관리자 비밀번호 초기화 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청 데이터 검증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (비밀번호 초기화 실패)")
    })
    ResponseEntity<ApiResponse<String>> resetPasswordByAdmin(Long userId, AdminPasswordResetRequest passwordResetDto);

    @Operation(
            summary = "회원 역할 변경",
            description = "관리자가 특정 사용자의 권한을 변경합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "역할을 변경할 사용자 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "역할 변경 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserTableResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (역할 변경 실패)")
    })
    ResponseEntity<ApiResponse<UserTableResponse>> updateUserRole(Long userId, UserRoleUpdateRequest request);

    @Operation(
            summary = "회원 삭제",
            description = "관리자가 회원 계정을 삭제(비활성화)합니다.",
            parameters = {
                    @Parameter(name = "userId", description = "삭제할 사용자 식별자", in = ParameterIn.PATH, required = true)
            }
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 오류 (회원 삭제 실패)")
    })
    ResponseEntity<ApiResponse<Object>> deleteUser(Long userId);
}
