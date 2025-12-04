package com.workhub.userTable.controller;

import com.workhub.global.response.ApiResponse;
import com.workhub.userTable.dto.AdminPasswordResetRequest;
import com.workhub.userTable.dto.UserPasswordChangeRequest;
import com.workhub.userTable.dto.UserRoleUpdateRequest;
import com.workhub.userTable.dto.UserTableResponse;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import com.workhub.global.security.CustomUserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    @DisplayName("본인이 비밀번호 변경을 요청하면 서비스가 호출되고 성공 응답을 반환한다")
    void updatePassword_success() {
        CustomUserDetails userDetails = new CustomUserDetails(sampleUserWithId(1L));
        UserPasswordChangeRequest request = new UserPasswordChangeRequest("Current!234", "NewPass!234");

        ResponseEntity<ApiResponse<String>> response = userController.updatePassword(userDetails, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo("비밀번호 재설정 완료");
        assertThat(response.getBody().getMessage()).isEqualTo("비밀번호 재설정 요청 성공");
        verify(userService).changePassword(1L, request);
    }

    @Test
    @DisplayName("관리자가 비밀번호 초기화를 요청하면 성공 메시지를 반환한다")
    void resetPasswordByAdmin_success() {
        AdminPasswordResetRequest request = new AdminPasswordResetRequest("TempPass!234");

        ResponseEntity<ApiResponse<String>> response = userController.resetPasswordByAdmin(2L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isEqualTo("관리자 비밀번호 초기화 완료");
        assertThat(response.getBody().getMessage()).isEqualTo("관리자가 비밀번호를 초기화했습니다.");
        verify(userService).resetPassword(2L, request);
    }

    @Test
    @DisplayName("관리자가 회원 역할을 성공적으로 변경하면 200 응답을 반환한다")
    void updateUserRole_success() {
        UserTable updatedUser = UserTable.builder()
                .loginId("testUser")
                .password("encoded")
                .email("user@test.com")
                .phone("01012345678")
                .role(UserRole.CLIENT)
                .status(Status.ACTIVE)
                .companyId(1L)
                .build();

        ReflectionTestUtils.setField(updatedUser, "userId", 1L);
        UserTableResponse responseDto = UserTableResponse.from(updatedUser);
        when(userService.updateRole(anyLong(), any(UserRole.class))).thenReturn(responseDto);

        ResponseEntity<ApiResponse<UserTableResponse>> response = userController.updateUserRole(
                1L,
                new UserRoleUpdateRequest(UserRole.CLIENT)
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().userId()).isEqualTo(1L);
        verify(userService).updateRole(1L, UserRole.CLIENT);
    }

    @Test
    @DisplayName("관리자가 회원을 삭제하면 성공 메시지를 반환한다")
    void deleteUser_success() {
        ResponseEntity<ApiResponse<Object>> response = userController.deleteUser(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("회원이 비활성화되었습니다.");
        verify(userService).deleteUser(2L);
    }

    private UserTable sampleUserWithId(Long userId) {
        UserTable user = UserTable.builder()
                .loginId("testUser")
                .password("encoded")
                .email("user@test.com")
                .phone("01012345678")
                .role(UserRole.CLIENT)
                .status(Status.ACTIVE)
                .companyId(1L)
                .build();

        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }
}
