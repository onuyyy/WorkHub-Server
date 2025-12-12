package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.user.response.UserDetailResponse;
import com.workhub.userTable.dto.user.response.UserListResponse;
import com.workhub.userTable.dto.user.request.UserLoginRecord;
import com.workhub.userTable.dto.user.request.AdminPasswordResetRequest;
import com.workhub.userTable.dto.user.request.UserPasswordChangeRequest;
import com.workhub.userTable.dto.user.request.UserRegisterRecord;
import com.workhub.userTable.dto.user.response.UserTableResponse;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("getUsers")
    class GetUsers {

        @Test
        @DisplayName("전체 사용자를 리스트 DTO로 반환한다")
        void success() {
            UserTable admin = user(1L, "admin", "admin@workhub.com", "01000000000", "Admin User", UserRole.ADMIN, Status.ACTIVE, 1L);
            UserTable client = user(2L, "client", "client@workhub.com", "01011112222", "Client User", UserRole.CLIENT, Status.SUSPENDED, 2L);
            given(userRepository.findAll()).willReturn(List.of(admin, client));

            List<UserListResponse> result = userService.getUsers();

            assertThat(result)
                    .containsExactly(UserListResponse.from(admin), UserListResponse.from(client));
        }
    }

    @Nested
    @DisplayName("getUser")
    class GetUser {

        @Test
        @DisplayName("단일 사용자를 상세 DTO로 반환한다")
        void success() {
            UserTable user = user(10L, "alpha", "alpha@workhub.com", "01099998888", "Alpha Tester", UserRole.DEVELOPER, Status.ACTIVE, 3L);
            given(userRepository.findById(10L)).willReturn(Optional.of(user));

            UserDetailResponse result = userService.getUser(10L);

            assertThat(result).isEqualTo(UserDetailResponse.from(user));
        }

        @Test
        @DisplayName("존재하지 않으면 런타임 예외를 던진다")
        void fail_notFound() {
            given(userRepository.findById(5L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(5L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(ErrorCode.USER_NOT_EXISTS.getMessage());
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("정상 인증에 성공하면 Authentication을 반환한다")
        void success() {
            Authentication authentication = mock(Authentication.class);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);

            Authentication result = userService.login(new UserLoginRecord("admin", "Password!234"));

            assertThat(result).isSameAs(authentication);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("인증 매니저가 실패를 던지면 BusinessException을 발생시킨다")
        void fail_invalidCredentials() {
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("bad"));

            assertThatThrownBy(() -> userService.login(new UserLoginRecord("admin", "wrong")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {
        @Test
        @DisplayName("존재하는 사용자는 그대로 반환한다")
        void success() {
            UserTable user = sampleUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            UserTable result = userService.getUserById(1L);

            assertThat(result).isEqualTo(user);
        }

        @Test
        @DisplayName("없는 사용자는 예외를 던진다")
        void fail_notFound() {
            given(userRepository.findById(5L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(5L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.USER_NOT_EXISTS);
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("중복 검증 통과 시 암호화된 비밀번호로 저장한다")
        void success() {
            UserRegisterRecord record = registerRecord();
            given(userRepository.existsByLoginId(record.loginId())).willReturn(false);
            given(userRepository.existsByEmail(record.email())).willReturn(false);
            given(passwordEncoder.encode(record.password())).willReturn("encoded");
            given(userRepository.save(any(UserTable.class))).willAnswer(invocation -> invocation.getArgument(0));

            UserTable saved = userService.register(record);

            assertThat(saved.getLoginId()).isEqualTo(record.loginId());
            assertThat(saved.getPassword()).isEqualTo("encoded");
            assertThat(saved.getRole()).isEqualTo(record.role());
            assertThat(saved.getStatus()).isEqualTo(Status.ACTIVE);
            verify(userRepository).save(any(UserTable.class));
        }

        @Test
        @DisplayName("로그인 아이디가 중복이면 즉시 예외")
        void fail_duplicateLoginId() {
            UserRegisterRecord record = registerRecord();
            given(userRepository.existsByLoginId(record.loginId())).willReturn(true);

            assertThatThrownBy(() -> userService.register(record))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ALREADY_REGISTERED_USER);

            verify(userRepository, never()).existsByEmail(anyString());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(UserTable.class));
        }

        @Test
        @DisplayName("이메일이 중복이면 저장하지 않고 예외")
        void fail_duplicateEmail() {
            UserRegisterRecord record = registerRecord();
            given(userRepository.existsByLoginId(record.loginId())).willReturn(false);
            given(userRepository.existsByEmail(record.email())).willReturn(true);

            assertThatThrownBy(() -> userService.register(record))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ALREADY_EXISTS__EMAIL);

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(UserTable.class));
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("현재 비밀번호가 일치하면 새 비밀번호로 변경한다")
        void success() {
            UserTable user = sampleUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("Plain!234", user.getPassword())).willReturn(true);
            given(passwordEncoder.encode("NewPass!234")).willReturn("encoded-new");

            userService.changePassword(1L, new UserPasswordChangeRequest("Plain!234", "NewPass!234"));

            assertThat(user.getPassword()).isEqualTo("encoded-new");
            verify(passwordEncoder).encode("NewPass!234");
        }

        @Test
        @DisplayName("현재 비밀번호가 다르면 예외를 던진다")
        void fail_invalidCurrentPassword() {
            UserTable user = sampleUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("wrong", user.getPassword())).willReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1L, new UserPasswordChangeRequest("wrong", "New!2345")))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.NOT_EQUAL_PASSWORD);

            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("resetPassword")
    class ResetPassword {

        @Test
        @DisplayName("관리자 요청이면 비밀번호를 즉시 새 값으로 설정한다")
        void success() {
            UserTable user = sampleUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(passwordEncoder.encode("NewPass!234")).willReturn("encoded-new");

            userService.resetPassword(1L, new AdminPasswordResetRequest("NewPass!234"));

            assertThat(user.getPassword()).isEqualTo("encoded-new");
            verify(passwordEncoder).encode("NewPass!234");
        }
    }

    @Nested
    @DisplayName("updateRole")
    class UpdateRole {

        @Test
        @DisplayName("사용자 역할을 새 권한으로 변경한다")
        void success() {
            UserTable user = sampleUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            UserTableResponse result = userService.updateRole(1L, UserRole.CLIENT);

            assertThat(result.role()).isEqualTo(UserRole.CLIENT);
    }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("사용자를 삭제하면 상태가 INACTIVE로 변경된다")
        void success() {
            UserTable user = sampleUser();
            given(userRepository.findById(1L)).willReturn(Optional.of(user));

            userService.deleteUser(1L);

            assertThat(user.getStatus()).isEqualTo(Status.INACTIVE);
            assertThat(user.getLastedAt()).isNotNull();
            verify(userRepository, never()).delete(any(UserTable.class));
        }
    }

    private UserTable user(Long userId, String loginId, String email, String phone, String userName, UserRole role, Status status, Long companyId) {
        return UserTable.builder()
                .userId(userId)
                .loginId(loginId)
                .password("encoded")
                .email(email)
                .phone(phone)
                .userName(userName)
                .role(role)
                .status(status)
                .companyId(companyId)
                .build();
    }

    private UserRegisterRecord registerRecord() {
        return new UserRegisterRecord(
                "freshUser",
                "Plain!234",
                "Fresh User",
                "fresh@workhub.com",
                "01012345678",
                1L,
                UserRole.CLIENT
        );
    }

    private UserTable sampleUser() {
        UserRegisterRecord register = new UserRegisterRecord(
                "admin",
                "encoded",
                "Admin User",
                "admin@workhub.com",
                "01000000000",
                1L,
                UserRole.ADMIN
        );

        return UserTable.of(register, "encoded");
    }
}
