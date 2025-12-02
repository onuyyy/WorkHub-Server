package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.dto.CsPostSearchRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCsPostService {

    private final CsPostService csPostService;
    private final ProjectService projectService;

    /**
     * CS POST 게시물을 조회합니다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @return CsPostResponse
     */
    public CsPostResponse findCsPost(Long projectId, Long csPostId) {

        projectService.validateCompletedProject(projectId);
        CsPost csPost = csPostService.findById(csPostId);

        if (!projectId.equals(csPost.getProjectId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_CS_POST);
        }

        List<CsPostFile> files = csPostService.findFilesByCsPostId(csPostId).stream()
                .filter(file -> file.getDeletedAt() == null)
                .toList();

        return CsPostResponse.from(csPost, files);
    }

    /**
     * CS POST 게시글 리스트를 조회한다.
     * @param projectId 프로젝트 식별자
     * @param searchType 검색 옵션
     * @param pageable 페이징 정보
     * @return Page<CsPostResponse>
     */
    public Page<CsPostResponse> findCsPosts(Long projectId, CsPostSearchRequest searchType, Pageable pageable) {

        projectService.validateCompletedProject(projectId);
        Page<CsPost> csPosts = csPostService.findCsPosts(searchType, pageable);

        return csPosts.map(CsPostResponse::from);
    }
}
