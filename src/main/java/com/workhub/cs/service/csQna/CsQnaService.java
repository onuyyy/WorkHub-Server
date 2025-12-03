package com.workhub.cs.service.csQna;

import com.workhub.cs.entity.CsQna;
import com.workhub.cs.repository.csQna.CsQnaRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CsQnaService {

    private final CsQnaRepository csQnaRepository;

    public CsQna save(CsQna csQna) {
        return csQnaRepository.save(csQna);
    }

    public CsQna findById(Long csQnaId) {
        return csQnaRepository.findById(csQnaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CS_QNA));
    }

    /**
     * 댓글이 있는지 검증하고, 댓글 작성자와 수정자가 맞는지 확인
     * @param csQnaId 댓글 식별자
     * @param userId 유저 식별자
     * @return CsQna
     */
    public CsQna findByCsQnaAndMatchedUserId(Long csQnaId, Long userId) {
        CsQna csQna = findById(csQnaId);
        if (!Objects.equals(csQna.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CS_QNA_USERID);
        }
        return csQna;
    }
}
