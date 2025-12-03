package com.workhub.cs.service;

import com.workhub.cs.entity.CsPost;
import com.workhub.cs.service.csPost.CsPostService;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CsPostAccessValidator {

    private final ProjectService projectService;
    private final CsPostService csPostService;

    /**
     * 프로젝트가 완료 상태인지 확인하고 게시글이 해당 프로젝트에 속하는지 검증한다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @return 검증된 게시글
     */
    public CsPost validateProjectAndGetPost(Long projectId, Long csPostId) {
        projectService.validateCompletedProject(projectId);
        CsPost post = csPostService.findById(csPostId);

        if (!projectId.equals(post.getProjectId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_CS_POST);
        }
        return post;
    }
}
