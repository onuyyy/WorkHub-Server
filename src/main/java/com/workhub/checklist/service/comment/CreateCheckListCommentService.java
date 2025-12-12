package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentFileRequest;
import com.workhub.checklist.dto.comment.CheckListCommentFileResponse;
import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCheckListCommentService {

    private final CheckListCommentService checkListCommentService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListService checkListService;

    /**
     * 프로젝트 해당하는 개발사/고객사와 관리자만이 댓글을 작성할 수 있다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param checkListId 체크리스트 식별자
     * @param checkListItemId 아이템 식별자
     * @param request 요청 폼
     * @return CheckListCommentResponse
     */
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

        List<CheckListCommentFileResponse> fileResponses = createCommentFiles(comment.getClCommentId(), request.files());

        return CheckListCommentResponse.from(comment, fileResponses);
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

    /**
     * 댓글 첨부파일 목록을 생성한다.
     * @param commentId 댓글 식별자
     * @param fileRequests 파일 요청 목록
     * @return CheckListCommentFileResponse 목록
     */
    private List<CheckListCommentFileResponse> createCommentFiles(Long commentId, List<CheckListCommentFileRequest> fileRequests) {
        if (fileRequests == null || fileRequests.isEmpty()) {
            return List.of();
        }

        return fileRequests.stream()
                .map(f -> CheckListItemCommentFile.of(commentId, f))
                .peek(checkListCommentService::saveCommentFile)
                .map(CheckListCommentFileResponse::from)
                .toList();
    }
}
