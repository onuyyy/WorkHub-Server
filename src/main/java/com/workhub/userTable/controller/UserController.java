package com.workhub.userTable.controller;

import com.workhub.file.service.UpdateProfileService;
import com.workhub.global.response.ApiResponse;
import com.workhub.global.security.CustomUserDetails;
import com.workhub.userTable.api.UserApi;
import com.workhub.userTable.dto.user.request.UserLoginRecord;
import com.workhub.userTable.dto.user.request.UserPasswordChangeRequest;
import com.workhub.userTable.dto.user.response.UserDetailResponse;
import com.workhub.userTable.dto.user.response.UserListResponse;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final UpdateProfileService profileService;

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

    @GetMapping("/list")
    @Override
    public List<UserListResponse> getUserTable(){
        return userService.getUsers();
    }


    @GetMapping("/{userId}")
    @Override
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUser(@PathVariable Long userId){
        UserDetailResponse response = userService.getUser(userId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/auth/passwordReset/confirm")
    @Override
    public ResponseEntity<ApiResponse<String>> updatePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                              @Valid @RequestBody UserPasswordChangeRequest passwordUpdateDto) {
        userService.changePassword(userDetails.getUserId(), passwordUpdateDto);
        return ApiResponse.success("비밀번호 재설정 완료", "비밀번호 재설정 요청 성공");
    }

    @PatchMapping("/profile")
    @Override
    public ResponseEntity<ApiResponse<String>> updateProfile(@RequestPart("file") MultipartFile file) {

        String profile = profileService.updateProfile(file);
        return ApiResponse.success(profile);

    }
}
