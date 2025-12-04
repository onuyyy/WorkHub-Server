package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.api.UserTableApi;
import com.workhub.userTable.dto.*;
import com.workhub.userTable.entity.UserTable;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.userTable.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController implements UserTableApi {

    private final UserService userService;

    @GetMapping
    public List<UserListResponse> getUserTable(){
        return userService.getUsers();
    }
    @GetMapping("/{userId}")
    public UserDetailResponse getUser(@PathVariable Long userId){
        return userService.getUser(userId);
    }

    @PostMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<String>> login(@RequestBody UserLoginRecord userLoginRecord,
                                                     HttpServletRequest request) {

        // 서비스에서 실제 인증 (authenticationManager.authenticate 호출)
        Authentication authentication = userService.login(userLoginRecord);

        // SecurityContext 생성해서 Authentication 넣기
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 세션에 SecurityContext 저장 → 다음 요청에서도 인증 유지
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        return ApiResponse.success("로그인 성공");
    }

    @PostMapping("/add/user")
    @Override
    public ResponseEntity<ApiResponse<UserTableResponse>> register(@RequestBody @Valid UserRegisterRecord registerRecord) {
        UserTable createdUser = userService.register(registerRecord);
        return ApiResponse.created(UserTableResponse.from(createdUser), "관리자가 계정을 생성했습니다.");
    }

    @PatchMapping("/auth/passwordReset/confirm")
    @Override
    public ResponseEntity<ApiResponse<String>> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @Valid @RequestBody UserPasswordChangeRequest passwordUpdateDto) {
        userService.changePassword(userDetails.getUserId(), passwordUpdateDto);
        return ApiResponse.success("비밀번호 재설정 완료", "비밀번호 재설정 요청 성공");
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

    @DeleteMapping("/{userId}")
    @Override
    public ResponseEntity<ApiResponse<Object>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.success(null, "회원이 비활성화되었습니다.");
    }
}
