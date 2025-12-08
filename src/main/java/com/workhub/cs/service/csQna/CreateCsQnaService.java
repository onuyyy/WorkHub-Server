package com.workhub.cs.service.csQna;

import com.workhub.cs.dto.csQna.CsQnaRequest;
import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCsQnaService {

    private final CsQnaService csQnaService;
    private final CsPostAccessValidator csPostAccessValidator;

    public CsQnaResponse create(Long projectId, Long csPostId, Long userId, CsQnaRequest csQnaRequest) {

        validateContent(csQnaRequest.qnaContent());
        csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId);

        Long parentQnaId = resolveParent(csPostId, csQnaRequest.parentQnaId());

        CsQna csQna = CsQna.of(csPostId, userId, parentQnaId, csQnaRequest.qnaContent());
        csQna = csQnaService.save(csQna);

        csQnaService.snapShotAndRecordHistory(csQna, csQna.getCsQnaId(), ActionType.CREATE);

        return CsQnaResponse.from(csQna);
    }

    /**
     * 내용이 없으면 INVALID_CS_QNA_CONTENT 예외
     * @param content 댓글 내용
     */
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CS_QNA_CONTENT);
        }
    }

    /**
     * 부모 댓글이 있는지 확인 후 없으면 null, 있으면 부모 댓글 ID 반환
     * @param csPostId 게시글 식별자
     * @param parentQnaId 부모 댓글 ID
     * @return Long
     */
    private Long resolveParent(Long csPostId, Long parentQnaId) {
        if (parentQnaId == null) {
            return null;
        }

        CsQna parent = csQnaService.findById(parentQnaId);
        if (!csPostId.equals(parent.getCsPostId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CS_QNA_POST);
        }

        return parent.getCsQnaId();
    }
}
