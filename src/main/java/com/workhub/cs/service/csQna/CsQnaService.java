package com.workhub.cs.service.csQna;

import com.workhub.cs.entity.CsQna;
import com.workhub.cs.repository.csQna.CsQnaRepository;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class CsQnaService {

    private final CsQnaRepository csQnaRepository;

    /**
     * CS QNA 엔티티를 저장한다.
     */
    public CsQna save(CsQna csQna) {
        return csQnaRepository.save(csQna);
    }

    /**
     * 식별자로 CS QNA를 조회한다.
     */
    public CsQna findById(Long csQnaId) {
        return csQnaRepository.findById(csQnaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CS_QNA));
    }

    /**
     * 댓글이 있는지 검증하고, 댓글 작성자와 수정자가 맞는지 확인한다.
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

    /**
     * CS 게시글의 모든 댓글을 조회한다 (페이징 없이).
     * @param csPostId 게시글 식별자
     * @return List<CsQna>
     */
    public List<CsQna> findAllByCsPostId(Long csPostId) {
        return csQnaRepository.findAllByCsPostId(csPostId);
    }

    /**
     * CS 게시글의 최상위 댓글을 페이징하여 조회한다.
     * @param csPostId 게시글 식별자
     * @param pageable 페이징 정보
     * @return Page<CsQna>
     */
    public Page<CsQna> findCsQnasWithReplies(Long csPostId, Pageable pageable) {
        return csQnaRepository.findCsQnasWithReplies(csPostId, pageable);
    }

    /**
     * 특정 댓글의 모든 자식 댓글(답글)을 조회한다.
     * @param parentQnaId 부모 댓글 식별자
     * @return List<CsQna>
     */
    public List<CsQna> findByParentQnaId(Long parentQnaId) {
        return csQnaRepository.findByParentQnaId(parentQnaId);
    }
}
