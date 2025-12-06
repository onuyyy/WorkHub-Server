package com.workhub.cs.service.csQna;

import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCsQnaService {

    private final CsQnaService csQnaService;
    private final CsPostAccessValidator csPostAccessValidator;
    private final HistoryRecorder historyRecorder;

    /**
     * CS 게시글의 댓글을 삭제한다. 댓글 삭제 시 모든 자식 댓글(답글)도 함께 삭제된다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param csQnaId 댓글 식별자
     * @param userId 사용자 식별자
     * @return 삭제된 댓글 id
     */
    public Long delete(Long projectId, Long csPostId, Long csQnaId, Long userId) {
        csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId);

        CsQna csQna = csQnaService.findByCsQnaAndMatchedUserId(csQnaId, userId);

        if (!csPostId.equals(csQna.getCsPostId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CS_QNA_POST);
        }

        if (csQna.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_CS_QNA);
        }

        deleteWithChildren(csQna);

        return csQna.getCsQnaId();
    }

    /**
     * 댓글과 모든 자식 댓글을 재귀적으로 삭제한다.
     * @param csQna 삭제할 댓글
     */
    private void deleteWithChildren(CsQna csQna) {
        List<CsQna> children = csQnaService.findByParentQnaId(csQna.getCsQnaId());

        for (CsQna child : children) {
            deleteWithChildren(child);
        }

        historyRecorder.recordHistory(HistoryType.CS_QNA, csQna.getCsQnaId(), ActionType.DELETE, csQna.getQnaContent());

        csQna.markDeleted();
    }
}
