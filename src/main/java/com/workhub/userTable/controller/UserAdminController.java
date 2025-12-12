package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.UserAdminApi;
import com.workhub.userTable.dto.user.request.AdminPasswordResetRequest;
import com.workhub.userTable.dto.user.request.UserRegisterRecord;
import com.workhub.userTable.dto.user.request.UserRoleUpdateRequest;
import com.workhub.userTable.dto.user.response.UserTableResponse;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController implements UserAdminApi {

    private final UserService userService;

    @PostMapping("/add/user")
    @Override
    public ResponseEntity<ApiResponse<UserTableResponse>> register(@RequestBody @Valid UserRegisterRecord registerRecord) {
        UserTable createdUser = userService.register(registerRecord);
        return ApiResponse.created(UserTableResponse.from(createdUser), "관리자가 계정을 생성했습니다.");
    }

    @PatchMapping("/{userId}/password/reset")
    @Override
    public ResponseEntity<ApiResponse<String>> resetPasswordByAdmin(@PathVariable Long userId,
                                                    @Valid @RequestBody AdminPasswordResetRequest passwordResetDto) {
        userService.resetPassword(userId, passwordResetDto);
        return ApiResponse.success("관리자 비밀번호 초기화 완료", "관리자가 비밀번호를 초기화했습니다.");
    }

    @PatchMapping("/{userId}/role")
    @Override
    public ResponseEntity<ApiResponse<UserTableResponse>> updateUserRole(@PathVariable Long userId,
                                                                         @Valid @RequestBody UserRoleUpdateRequest request) {
        UserTableResponse updatedUser = userService.updateRole(userId, request.role());
        return ApiResponse.success(updatedUser, "회원 역할이 변경되었습니다.");
    }

    @DeleteMapping("/delete/{userId}")
    @Override
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.success(null, "회원이 비활성화되었습니다.");
    }
}
