package com.workhub.global.util;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;

/**
 * 상태 변경 검증을 위한 유틸리티 클래스
 */
public class StatusValidator {

    private StatusValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 현재 상태와 새로운 상태가 동일한지 검증합니다.
     * 동일한 경우 BusinessException을 발생시킵니다.
     *
     * @param currentStatus 현재 상태
     * @param newStatus 변경할 상태
     * @param <T> Enum 타입
     * @throws BusinessException 상태가 동일한 경우
     */
    public static <T extends Enum<T>> void validateStatusChange(T currentStatus, T newStatus) {
        if (currentStatus == newStatus) {
            throw new BusinessException(ErrorCode.STATUS_ALREADY_SET);
        }
    }
}