package com.workhub.cs.service.csPost;

import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.global.util.SecurityUtil;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCsPostService {

    private final CsPostService csPostService;
    private final ProjectService projectService;
    private final HistoryRecorder historyRecorder;

    /**
     * 프로젝트 소속을 검증한 뒤 CS POST를 삭제한다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param userId 삭제 요청 사용자 식별자
     * @return 삭제된 게시글 id
     */
    public Long delete(Long projectId, Long csPostId, Long userId) {

        projectService.validateCompletedProject(projectId);
        CsPost csPost = csPostService.findById(csPostId);

        if (!projectId.equals(csPost.getProjectId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_CS_POST);
        }

        if (csPost.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_CS_POST);
        }

        csPost.validateProject(projectId);
        validateDeletionPermission(csPost, userId);

        List<CsPostFile> csPostFiles = csPostService.findFilesByCsPostId(csPostId);

        csPostService.snapShotAndRecordHistory(csPost, csPost.getCsPostId(), ActionType.DELETE);

        csPost.markDeleted();
        markFilesDeleted(csPostFiles);

        return csPost.getCsPostId();
    }

    private void validateDeletionPermission(CsPost csPost, Long userId) {
        boolean isAdmin = SecurityUtil.hasRole("ADMIN");
        boolean isAuthor = csPost.getUserId().equals(userId);

        if (!isAdmin && !isAuthor) {
            throw new BusinessException(ErrorCode.FORBIDDEN_CS_POST_DELETE);
        }
    }

    private void markFilesDeleted(List<CsPostFile> csPostFiles) {
        for (CsPostFile csPostFile : csPostFiles) {
            if (!csPostFile.isDeleted()) {
                csPostFile.markDeleted();
            }
        }
    }
}
