package com.workhub.cs.service.csQna;

import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCsQnaService {

    private final CsQnaService csQnaService;
    private final CsPostAccessValidator csPostAccessValidator;

    /**
     * CS 게시글의 댓글 목록을 계층 구조로 조회한다.
     * 최상위 댓글만 페이징하며, 각 댓글의 답글은 모두 포함된다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param pageable 페이징 정보 (최상위 댓글 기준)
     * @return Page<CsQnaResponse> 계층 구조로 구성된 댓글 목록
     */
    public Page<CsQnaResponse> findCsQnas(Long projectId, Long csPostId, Pageable pageable) {
        csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId);

        // 1. 모든 댓글 조회 (부모 댓글 + 답글)
        List<CsQna> allComments = csQnaService.findAllByCsPostId(csPostId);

        // 2. 최상위 댓글만 페이징
        Page<CsQna> topLevelComments = csQnaService.findCsQnasWithReplies(csPostId, pageable);

        // 3. 계층 구조로 변환
        List<CsQnaResponse> hierarchicalComments = buildHierarchy(
                topLevelComments.getContent(),
                allComments
        );

        return new PageImpl<>(
                hierarchicalComments,
                pageable,
                topLevelComments.getTotalElements()
        );
    }

    /**
     * 댓글 목록을 계층 구조로 변환한다.
     * @param topLevelComments 최상위 댓글 목록
     * @param allComments 모든 댓글 목록 (부모 + 자식)
     * @return 계층 구조로 구성된 댓글 목록
     */
    private List<CsQnaResponse> buildHierarchy(List<CsQna> topLevelComments, List<CsQna> allComments) {
        // parentQnaId를 키로 하는 맵 생성 (자식 댓글 그룹화)
        Map<Long, List<CsQna>> childrenMap = allComments.stream()
                .filter(comment -> comment.getParentQnaId() != null)
                .collect(Collectors.groupingBy(CsQna::getParentQnaId));

        return topLevelComments.stream()
                .map(parent -> buildCommentWithChildren(parent, childrenMap))
                .collect(Collectors.toList());
    }

    /**
     * 재귀적으로 댓글과 그 자식 댓글들을 구성한다.
     * @param comment 현재 댓글
     * @param childrenMap 부모 ID를 키로 하는 자식 댓글 맵
     * @return 자식 댓글을 포함한 CsQnaResponse
     */
    private CsQnaResponse buildCommentWithChildren(CsQna comment, Map<Long, List<CsQna>> childrenMap) {
        List<CsQna> children = childrenMap.getOrDefault(comment.getCsQnaId(), new ArrayList<>());

        List<CsQnaResponse> childResponses = children.stream()
                .map(child -> buildCommentWithChildren(child, childrenMap))
                .collect(Collectors.toList());

        return CsQnaResponse.from(comment).withChildren(childResponses);
    }
}