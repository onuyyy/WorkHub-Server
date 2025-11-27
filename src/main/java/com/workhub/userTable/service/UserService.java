package com.workhub.userTable.service;

import com.workhub.userTable.dto.UserLoginRecord;
import com.workhub.userTable.dto.UserRegisterRecord;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.repository.UserRepository;
import com.workhub.userTable.entity.Status;
import com.workhub.userTable.entity.UserTable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

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


        UserTable userTable = UserTable.builder()
                .loginId(record.loginId())
                .password(passwordEncoder.encode(record.password()))
                .email(record.email())
                .phone(record.phone())
                .role(record.role())
                .status(Status.ACTIVE)
                .companyId(record.companyId())
                .build();
        
        return userRepository.save(userTable);
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
}
