package com.workhub.checklist.service.comment;

import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeleteCheckListCommentService {

    private final CheckListCommentService checkListCommentService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListService checkListService;
    private final FileService fileService;

    /**
     * 작성자 또는 관리자만이 댓글을 삭제할 수 있으며, 삭제 시 모든 자식 댓글도 함께 삭제된다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param checkListId 체크리스트 식별자
     * @param checkListItemId 체크리스트 항목 식별자
     * @param commentId 댓글 식별자
     * @return 삭제된 댓글 식별자
     */
    public Long delete(Long projectId,
                       Long nodeId,
                       Long checkListId,
                       Long checkListItemId,
                       Long commentId) {

        // 1. 삭제 대상 검증 및 조회
        CheckListItemComment comment = validateAndFindComment(
                projectId, nodeId, checkListId, checkListItemId, commentId);

        // 2. 댓글 및 자식 댓글 삭제
        List<String> filesToDelete = new ArrayList<>();
        deleteWithChildren(comment, filesToDelete);

        // 3. 파일 삭제
        deleteFilesFromS3(filesToDelete);

        return commentId;
    }

    // 삭제 가능 여부를 검증하고 대상 댓글을 조회한다.
    private CheckListItemComment validateAndFindComment(Long projectId,
                                                        Long nodeId,
                                                        Long checkListId,
                                                        Long checkListItemId,
                                                        Long commentId) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);

        CheckList checkList = checkListService.findById(checkListId);
        validateCheckListBelongsToNode(nodeId, checkList);

        CheckListItem checkListItem = checkListService.findCheckListItem(checkListItemId);
        validateItemBelongsToCheckList(checkListId, checkListItem);

        CheckListItemComment comment = checkListCommentService.findById(commentId);
        validateCommentBelongsToItem(checkListItemId, comment);
        validateCommentNotDeleted(comment);
        checkListAccessValidator.validateAdminOrCommentOwner(comment.getUserId());

        return comment;
    }

    // 체크리스트가 요청한 노드에 속하는지 검증한다.
    private void validateCheckListBelongsToNode(Long nodeId, CheckList checkList) {
        if (!nodeId.equals(checkList.getProjectNodeId())) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST);
        }
    }

    // 항목이 지정한 체크리스트에 속하는지 검증한다.
    private void validateItemBelongsToCheckList(Long checkListId, CheckListItem checkListItem) {
        if (!checkListId.equals(checkListItem.getCheckListId())) {
            throw new BusinessException(ErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST);
        }
    }

    // 댓글이 요청한 항목에 속하는지 확인한다.
    private void validateCommentBelongsToItem(Long checkListItemId, CheckListItemComment comment) {
        if (!checkListItemId.equals(comment.getCheckListItemId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CHECK_LIST_ITEM_COMMENT);
        }
    }

    // 이미 삭제된 댓글인지 확인한다.
    private void validateCommentNotDeleted(CheckListItemComment comment) {
        if (comment.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_CHECK_LIST_ITEM_COMMENT);
        }
    }

    // 댓글과 모든 자식 댓글을 재귀적으로 삭제하고 파일을 수집한다.
    private void deleteWithChildren(CheckListItemComment comment, List<String> filesToDelete) {
        List<CheckListItemComment> children = checkListCommentService.findChildrenByParentId(comment.getClCommentId());
        for (CheckListItemComment child : children) {
            deleteWithChildren(child, filesToDelete);
        }

        deleteCommentFiles(comment.getClCommentId(), filesToDelete);
        comment.markDeleted();
        checkListService.snapShotAndRecordHistory(comment, comment.getClCommentId(), ActionType.DELETE);
    }

    // 댓글에 연결된 파일을 삭제하고 관리 중인 파일은 S3 삭제 대상으로 모은다.
    private void deleteCommentFiles(Long commentId, List<String> filesToDelete) {
        List<CheckListItemCommentFile> existingFiles =
                checkListCommentService.findCommentFilesByCommentId(commentId);

        if (existingFiles.isEmpty()) {
            return;
        }

        existingFiles.forEach(file -> {
            if (file.isManagedFile()) {
                filesToDelete.add(file.getFileUrl());
            }
        });
        checkListCommentService.deleteCommentFiles(existingFiles);
    }

    // S3에서 파일을 삭제한다.
    private void deleteFilesFromS3(List<String> filesToDelete) {
        if (filesToDelete == null || filesToDelete.isEmpty()) {
            return;
        }

        try {
            fileService.deleteFiles(filesToDelete);
        } catch (Exception deleteException) {
            log.error("댓글 삭제 중 파일 삭제 오류 발생: {}", filesToDelete, deleteException);
        }
    }
}
