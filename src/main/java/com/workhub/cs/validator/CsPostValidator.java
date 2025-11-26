package com.workhub.cs.validator;

import com.workhub.cs.repository.CsPostRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CsPostValidator {

    private final CsPostRepository csPostRepository;

    public void validateExists(Long csPostId){
        if (!csPostRepository.existsById(csPostId)){
            throw new BusinessException(ErrorCode.NOT_EXISTS_CS_POST);
        }
    }
}
