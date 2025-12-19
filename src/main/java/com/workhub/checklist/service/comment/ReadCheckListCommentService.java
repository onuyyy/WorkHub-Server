package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentFileResponse;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.port.AuthorLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadCheckListCommentService {

    private final CheckListCommentService checkListCommentService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListService checkListService;
    private final AuthorLookupPort authorLookupPort;

    /**
     * 체크리스트 항목에 달린 모든 댓글을 검증 후 계층 구조로 조회한다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param checkListId 체크리스트 식별자
     * @param checkListItemId 체크리스트 항목 식별자
     * @return 부모-자식 관계가 구성된 댓글 응답 목록
     */
    public List<CheckListCommentResponse> findComments(Long projectId, Long nodeId, Long checkListId, Long checkListItemId) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);

        CheckList checkList = checkListService.findById(checkListId);
        validateCheckListBelongsToNode(nodeId, checkList);

        CheckListItem checkListItem = checkListService.findCheckListItem(checkListItemId);
        validateItemBelongsToCheckList(checkListId, checkListItem);

        List<CheckListItemComment> allComments = checkListCommentService.findAllByCheckListItemId(checkListItemId);
        List<CheckListItemComment> topLevelComments = checkListCommentService.findTopLevelCommentsByCheckListItemId(checkListItemId);

        return buildHierarchy(topLevelComments, allComments);
    }

    // 체크리스트가 요청한 노드에 속하는지 확인한다.
    private void validateCheckListBelongsToNode(Long nodeId, CheckList checkList) {
        if (!nodeId.equals(checkList.getProjectNodeId())) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST);
        }
    }

    // 체크리스트 아이템이 지정한 체크리스트에 속하는지 확인한다.
    private void validateItemBelongsToCheckList(Long checkListId, CheckListItem checkListItem) {
        if (!checkListItem.getCheckListId().equals(checkListId)) {
            throw new BusinessException(ErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST);
        }
    }

    // 전체 댓글을 부모 기준으로 묶어 트리 구조 응답을 생성한다.
    private List<CheckListCommentResponse> buildHierarchy(List<CheckListItemComment> topLevelComments, List<CheckListItemComment> allComments) {
        Map<Long, List<CheckListItemComment>> childrenMap = allComments.stream()
                .filter(comment -> comment.getParentClCommentId() != null)
                .collect(Collectors.groupingBy(CheckListItemComment::getParentClCommentId));

        Set<Long> userIds = allComments.stream().map(CheckListItemComment::getUserId).collect(Collectors.toSet());
        Map<Long, String> userNameMap = authorLookupPort.findByUserIds(userIds.stream().toList()).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().userName()));

        Map<Long, List<CheckListItemCommentFile>> filesMap = allComments.stream()
                .map(CheckListItemComment::getClCommentId)
                .collect(Collectors.toMap(
                        commentId -> commentId,
                        commentId -> checkListCommentService.findCommentFilesByCommentId(commentId)
                ));

        return topLevelComments.stream()
                .map(parent -> buildCommentWithChildren(parent, childrenMap, userNameMap, filesMap))
                .collect(Collectors.toList());
    }

    // 단일 댓글에 자식 응답을 재귀적으로 붙여 완성한다.
    private CheckListCommentResponse buildCommentWithChildren(
            CheckListItemComment comment,
            Map<Long, List<CheckListItemComment>> childrenMap,
            Map<Long, String> userNameMap,
            Map<Long, List<CheckListItemCommentFile>> filesMap
    ) {
        List<CheckListItemComment> children = childrenMap.getOrDefault(comment.getClCommentId(), new ArrayList<>());

        List<CheckListCommentResponse> childResponses = children.stream()
                .map(child -> buildCommentWithChildren(child, childrenMap, userNameMap, filesMap))
                .collect(Collectors.toList());

        String userName = userNameMap.get(comment.getUserId());
        List<CheckListCommentFileResponse> files = filesMap.getOrDefault(comment.getClCommentId(), List.of())
                .stream()
                .map(CheckListCommentFileResponse::from)
                .collect(Collectors.toList());

        return CheckListCommentResponse.from(comment, userName, files).withChildren(childResponses);
    }
}
