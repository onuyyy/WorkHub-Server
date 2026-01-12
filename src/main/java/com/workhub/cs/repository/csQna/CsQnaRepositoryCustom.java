package com.workhub.cs.repository.csQna;

import com.workhub.cs.entity.CsQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CsQnaRepositoryCustom {

    /**
     * CS 게시글의 모든 댓글을 조회한다 (부모 댓글 + 답글 모두 포함).
     * @param csPostId 게시글 식별자
     * @param pageable 페이징 정보 (최상위 댓글 기준)
     * @return Page<CsQna> 최상위 댓글만 페이징, 각 댓글의 답글은 전체 포함
     */
    Page<CsQna> findCsQnasWithReplies(Long csPostId, Pageable pageable);

    /**
     * CS 게시글의 모든 댓글을 조회한다 (페이징 없이).
     * @param csPostId 게시글 식별자
     * @return List<CsQna>
     */
    List<CsQna> findAllByCsPostId(Long csPostId);
}