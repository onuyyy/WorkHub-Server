package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.dto.csPost.CsPostSearchRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.port.AuthorLookupPort;
import com.workhub.cs.port.dto.AuthorProfile;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCsPostService {

    private final CsPostService csPostService;
    private final ProjectService projectService;
    private final AuthorLookupPort authorLookupPort;

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

        String userName = authorLookupPort.findByUserId(csPost.getUserId())
                .map(AuthorProfile::userName)
                .orElse(null);

        return CsPostResponse.from(csPost, files, userName);
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
        Page<CsPost> csPosts = csPostService.findCsPosts(projectId, searchType, pageable);

        Map<Long, AuthorProfile> authorMap = loadAuthors(csPosts);

        return csPosts.map(post -> {
            AuthorProfile author = authorMap.get(post.getUserId());
            String userName = (author != null) ? author.userName() : null;
            return CsPostResponse.from(post, userName);
        });
    }

    private Map<Long, AuthorProfile> loadAuthors(Page<CsPost> csPosts) {
        List<Long> userIds = csPosts.getContent().stream()
                .map(CsPost::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return authorLookupPort.findByUserIds(userIds);
    }
}
