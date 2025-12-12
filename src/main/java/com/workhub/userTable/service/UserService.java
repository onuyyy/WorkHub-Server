package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.user.request.AdminPasswordResetRequest;
import com.workhub.userTable.dto.user.request.UserPasswordChangeRequest;
import com.workhub.userTable.dto.user.response.*;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserRole;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public List<UserListResponse> getUsers(){
        return userRepository.findAll().stream()
                .map(UserListResponse::from)
                .toList();
    }

    public UserDetailResponse getUser(Long userId){
        UserTable userTable = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));
        return UserDetailResponse.from(userTable);

    }

    public UserTable getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXISTS));
    }

    public Authentication login(UserLoginRecord userLoginRecord) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                userLoginRecord.loginId(),
                userLoginRecord.password()
        );

        try {
            return authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException exception) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_CREDENTIALS);
        }
    }

    @Transactional
    public UserTable register(UserRegisterRecord record) {
        validateLoginId(record.loginId());
        validateEmail(record.email());

        UserTable userTable = UserTable.of(
                record,
                passwordEncoder.encode(record.password())
        );

        return userRepository.save(userTable);
    }

    @Transactional
    public void changePassword(Long targetUserId, UserPasswordChangeRequest passwordChangeRequest) {
        UserTable userTable = getUserById(targetUserId);
        if (!passwordEncoder.matches(passwordChangeRequest.currentPassword(), userTable.getPassword())) {
            throw new BusinessException(ErrorCode.NOT_EQUAL_PASSWORD);
        }

        userTable.updatePassword(passwordEncoder.encode(passwordChangeRequest.newPassword()));
    }

    @Transactional
    public void resetPassword(Long targetUserId, AdminPasswordResetRequest passwordResetRequest) {
        UserTable userTable = getUserById(targetUserId);
        userTable.updatePassword(passwordEncoder.encode(passwordResetRequest.newPassword()));
    }

    @Transactional
    public UserTableResponse updateRole(Long userId, UserRole role) {
        UserTable userTable = getUserById(userId);
        userTable.updateRole(role);
        return UserTableResponse.from(userTable);
    }

    @Transactional
    public void deleteUser(Long userId) {
        UserTable userTable = getUserById(userId);
        userTable.updateStatus(Status.INACTIVE);
    }

    private void validateLoginId(String loginId) {
        if (userRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.ALREADY_REGISTERED_USER);
        }
    }

    private void validateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS__EMAIL);
        }
    }

    public Map<Long, UserTable> getUserMapByUserIdIn(List<Long> userIds) {
        return userRepository.findMapByUserIdIn(userIds);
    }

    public List<UserNameResponse> getUserMapByCompanyIdIn(Long companyId) {

        List<UserTable> userNames = userRepository.findMapByCompanyIdIn(companyId);

        return userNames.stream()
                .map(UserNameResponse::from)
                .toList();
    }
}
