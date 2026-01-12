package com.workhub.userTable.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import com.workhub.userTable.dto.email.EmailVerificationConfirmRequest;
import com.workhub.userTable.dto.user.request.UpdatePhoneRequest;
import com.workhub.userTable.entity.UserTable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserService {

    private final EmailVerificationService emailVerificationService;
    private final UserService userService;

    @Transactional
    public void updatePhone(UpdatePhoneRequest request) {

        Long loginUser = SecurityUtil.getCurrentUserIdOrThrow();
        UserTable user = userService.getUserById(loginUser);

        user.updatePhone(request.phone());

    }

    @Transactional
    public void verifyCodeAndUpdateEmail(EmailVerificationConfirmRequest request) {

        boolean verifyCode = emailVerificationService.verifyCode(request.email(), request.code());
        if (!verifyCode) {
            throw new BusinessException(ErrorCode.NOT_EQUAL_CODE);
        }

        UserTable user = userService.getUserById(SecurityUtil.getCurrentUserIdOrThrow());
        user.updateEmail(request.email());

    }
}
