package com.workhub.cs.service.csQna;

import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.dto.csQna.CsQnaUpdateRequest;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCsQnaService {

    private final CsQnaService csQnaService;
    private final CsPostAccessValidator csPostAccessValidator;
    private final HistoryRecorder historyRecorder;

    public CsQnaResponse update(Long projectId, Long csPostId, Long csQnaId, Long userId, CsQnaUpdateRequest request) {
        // 1. 댓글 조회 및 작성자 검증 (가장 구체적인 검증)
        CsQna csQna = csQnaService.findByCsQnaAndMatchedUserId(csQnaId, userId);

        // 2. 댓글이 해당 게시글에 속하는지 검증
        validateCsPostBelongs(csQna, csPostId);

        // 3. 프로젝트-게시글 관계 검증
        csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId);

        historyRecorder.recordHistory(HistoryType.CS_QNA, csQna.getCsQnaId(), ActionType.UPDATE, csQna.getQnaContent());

        csQna.updateContent(request.qnaContent());
        return CsQnaResponse.from(csQna);
    }

    /**
     * 해당 댓글이 실제 게시글에 속하는지 검증
     * @param csQna
     * @param csPostId
     */
    private void validateCsPostBelongs(CsQna csQna, Long csPostId) {
        if (!csQna.getCsPostId().equals(csPostId)) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CS_QNA_POST);
        }
    }


}
