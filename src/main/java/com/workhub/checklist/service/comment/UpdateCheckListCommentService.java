package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.dto.comment.CheckListCommentUpdateRequest;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListItemComment;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCheckListCommentService {

    private final CheckListCommentService checkListCommentService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListService checkListService;

    /**
     * 작성자 또는 관리자만이 댓글을 수정할 수 있다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param checkListId 체크리스트 식별자
     * @param checkListItemId 아이템 식별자
     * @param commentId 댓글 식별자
     * @param request 요청 폼
     * @return CheckListCommentResponse
     */
    public CheckListCommentResponse update(Long projectId,
                                           Long nodeId,
                                           Long checkListId,
                                           Long checkListItemId,
                                           Long commentId,
                                           CheckListCommentUpdateRequest request) {

        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);

        validateContent(request.content());

        CheckList checkList = checkListService.findById(checkListId);
        validateCheckListBelongsToNode(nodeId, checkList);

        CheckListItem checkListItem = checkListService.findCheckListItem(checkListItemId);
        validateItemBelongsToCheckList(checkListId, checkListItem);

        CheckListItemComment comment = checkListCommentService.findById(commentId);
        validateCommentBelongsToItem(checkListItemId, comment);
        checkListAccessValidator.validateAdminOrCommentOwner(comment.getUserId());

        comment.updateContent(request.content());
        checkListService.snapShotAndRecordHistory(comment, comment.getClCommentId(), ActionType.UPDATE);

        return CheckListCommentResponse.from(comment);
    }

    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_ITEM_COMMENT_CONTENT);
        }
    }

    private void validateCheckListBelongsToNode(Long nodeId, CheckList checkList) {
        if (!nodeId.equals(checkList.getProjectNodeId())) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST);
        }
    }

    private void validateItemBelongsToCheckList(Long checkListId, CheckListItem checkListItem) {
        if (!checkListId.equals(checkListItem.getCheckListId())) {
            throw new BusinessException(ErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST);
        }
    }

    private void validateCommentBelongsToItem(Long checkListItemId, CheckListItemComment comment) {
        if (!checkListItemId.equals(comment.getCheckListItemId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CHECK_LIST_ITEM_COMMENT);
        }
    }

}
