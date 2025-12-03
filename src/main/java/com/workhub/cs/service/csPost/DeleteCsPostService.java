package com.workhub.cs.service.csPost;

import com.workhub.cs.entity.CsPost;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCsPostService {

    private final CsPostService csPostService;
    private final ProjectService projectService;

    /**
     * 프로젝트 소속을 검증한 뒤 CS POST를 삭제한다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @return 삭제된 게시글 id
     */
    public Long delete(Long projectId, Long csPostId) {

        projectService.validateCompletedProject(projectId);
        CsPost csPost = csPostService.findById(csPostId);

        if (!projectId.equals(csPost.getProjectId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_CS_POST);
        }

        if (csPost.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_CS_POST);
        }

        csPost.validateProject(projectId);
        csPost.markDeleted();

        return csPost.getCsPostId();
    }
}
