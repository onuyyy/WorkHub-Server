package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.UserLoginRecord;
import com.workhub.userTable.dto.UserRegisterRecord;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

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

    @Test
    @DisplayName("로그인에 성공하면 인증 객체를 반환한다")
    void login_success() {
        Authentication authentication = mock(Authentication.class);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);

        Authentication result = userService.login(new UserLoginRecord("admin", "password"));

        assertThat(result).isSameAs(authentication);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("로그인 인증에 실패하면 BusinessException이 발생한다")
    void login_invalidCredentials() {
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> userService.login(new UserLoginRecord("admin", "wrong")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CREDENTIALS);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("ID로 사용자 조회 시 존재한다면 그대로 반환한다")
    void getUserById_success() {
        UserTable mockUser = sampleUser();
        given(userRepository.findById(1L)).willReturn(Optional.of(mockUser));

        UserTable found = userService.getUserById(1L);

        assertThat(found).isEqualTo(mockUser);
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("ID로 사용자 조회 시 없으면 예외가 발생한다")
    void getUserById_notFound() {
        given(userRepository.findById(5L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(5L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("관리자가 회원을 등록하면 중복 검증 후 암호화된 비밀번호로 저장한다")
    void register_success() {
        UserRegisterRecord record = new UserRegisterRecord(
                "freshUser",
                "Plain!234",
                "fresh@workhub.com",
                "01012345678",
                1L,
                UserRole.CLIENT
        );

        given(userRepository.existsByLoginId("freshUser")).willReturn(false);
        given(userRepository.existsByEmail("fresh@workhub.com")).willReturn(false);
        given(passwordEncoder.encode("Plain!234")).willReturn("encoded");
        given(userRepository.save(any(UserTable.class))).willAnswer(invocation -> invocation.getArgument(0));

        UserTable created = userService.register(record);

        assertThat(created.getLoginId()).isEqualTo("freshUser");
        assertThat(created.getPassword()).isEqualTo("encoded");
        assertThat(created.getRole()).isEqualTo(UserRole.CLIENT);
        assertThat(created.getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(created.getCompanyId()).isEqualTo(1L);

        verify(passwordEncoder).encode("Plain!234");
        verify(userRepository).save(any(UserTable.class));
    }

    @Test
    @DisplayName("로그인 아이디가 중복이면 즉시 예외를 던지고 다음 검증을 하지 않는다")
    void register_duplicateLoginId() {
        UserRegisterRecord record = new UserRegisterRecord(
                "duplicate",
                "Plain!234",
                "dup@workhub.com",
                "01012345678",
                1L,
                UserRole.CLIENT
        );

        given(userRepository.existsByLoginId("duplicate")).willReturn(true);

        assertThatThrownBy(() -> userService.register(record))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_REGISTERED_USER);

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(UserTable.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("이메일이 중복이면 예외가 발생하며 암호화를 수행하지 않는다")
    void register_duplicateEmail() {
        UserRegisterRecord record = new UserRegisterRecord(
                "freshUser",
                "Plain!234",
                "dup@workhub.com",
                "01012345678",
                1L,
                UserRole.CLIENT
        );

        given(userRepository.existsByLoginId("freshUser")).willReturn(false);
        given(userRepository.existsByEmail("dup@workhub.com")).willReturn(true);

        assertThatThrownBy(() -> userService.register(record))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ALREADY_EXISTS__EMAIL);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(UserTable.class));
    }

    private UserTable sampleUser() {
        return UserTable.builder()
                .userId(1L)
                .loginId("admin")
                .password("encoded")
                .email("admin@workhub.com")
                .phone("01000000000")
                .role(UserRole.ADMIN)
                .status(Status.ACTIVE)
                .companyId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
