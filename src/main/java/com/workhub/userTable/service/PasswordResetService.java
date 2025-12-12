package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.userTable.dto.PasswordResetConfirmRequest;
import com.workhub.userTable.dto.PasswordResetSendRequest;
import com.workhub.userTable.entity.UserTable;
import com.workhub.userTable.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendResetCode(PasswordResetSendRequest request) {
        UserTable user = findActiveUserByEmail(request.email());
        emailVerificationService.sendVerificationCode(user.getEmail(), user.getUserName());
    }

    @Transactional
    public void resetPassword(PasswordResetConfirmRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        boolean verified = emailVerificationService.verifyCode(normalizedEmail, request.verificationCode());
        if (!verified) {
            throw new BusinessException(ErrorCode.NOT_EQUAL_CODE);
        }

        UserTable user = findActiveUserByEmail(normalizedEmail);
        user.updatePassword(passwordEncoder.encode(request.newPassword()));
        emailVerificationService.consumeVerification(normalizedEmail);
    }

    private UserTable findActiveUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_EMAIL));
    }
}
