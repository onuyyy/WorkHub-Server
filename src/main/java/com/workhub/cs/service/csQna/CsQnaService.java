package com.workhub.cs.service.csQna;

import com.workhub.cs.entity.CsQna;
import com.workhub.cs.repository.csQna.CsQnaRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CsQnaService {

    private final CsQnaRepository csQnaRepository;

    /**
     * CS POST QNA 엔티티를 저장한다.
     */
    public CsQna save(CsQna csQna) {
        return csQnaRepository.save(csQna);
    }

    /**
     * CS POST QNA 엔티티를 찾는다.
     */
    public CsQna findById(Long csQnaId) {
        return csQnaRepository.findById(csQnaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CS_QNA));
    }
}
