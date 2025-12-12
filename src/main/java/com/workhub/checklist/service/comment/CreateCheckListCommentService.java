package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListItemComment;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCheckListCommentService {

    private final CheckListCommentService checkListCommentService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListService checkListService;

    public CheckListCommentResponse create(
            Long projectId, Long nodeId, Long checkListId, Long checkListItemId, CheckListCommentRequest request) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);

        validateContent(request.content());

        CheckList checkList = checkListService.findById(checkListId);
        validateCheckListBelongsToNode(nodeId, checkList);

        CheckListItem checkListItem = checkListService.findCheckListItem(checkListItemId);
        validateItemBelongsToCheckList(checkListId, checkListItem);

        Long parentCommentId = resolveParent(checkListItemId, request.patentClCommentId());
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();

        CheckListItemComment comment = CheckListItemComment.of(checkListItemId, userId, parentCommentId, request.content());
        comment = checkListCommentService.save(comment);
        checkListService.snapShotAndRecordHistory(comment, comment.getClCommentId(), ActionType.CREATE);

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

    private Long resolveParent(Long checkListItemId, Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        CheckListItemComment parentComment = checkListCommentService.findById(parentCommentId);
        if (!checkListItemId.equals(parentComment.getCheckListItemId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CHECK_LIST_ITEM_COMMENT);
        }
        return parentComment.getClCommentId();
    }
}
