package com.workhub.checklist.service;

import com.workhub.checklist.dto.CheckListItemCommentHistorySnapShot;
import com.workhub.checklist.dto.CheckListItemHistorySnapShot;
import com.workhub.checklist.entity.*;
import com.workhub.checklist.repository.CheckListItemRepository;
import com.workhub.checklist.repository.CheckListOptionFileRepository;
import com.workhub.checklist.repository.CheckListOptionRepository;
import com.workhub.checklist.repository.CheckListRepository;
import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckListService {

    private final CheckListRepository checkListRepository;
    private final CheckListItemRepository checkListItemRepository;
    private final CheckListOptionRepository checkListOptionRepository;
    private final CheckListOptionFileRepository checkListOptionFileRepository;
    private final HistoryRecorder historyRecorder;

    /**
     * CheckList 엔티티를 저장한다.
     */
    public CheckList saveCheckList(CheckList checkList) {
        return checkListRepository.save(checkList);
    }

    /**
     * 식별자로 CheckList를 조회한다.
     */
    public CheckList findById(Long checkListId) {
        return checkListRepository.findById(checkListId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST));
    }

    /**
     * CheckListItem 엔티티를 저장한다.
     */
    public CheckListItem saveCheckListItem(CheckListItem item) {
        return checkListItemRepository.save(item);
    }

    /**
     * CheckListOption 엔티티를 저장한다.
     */
    public CheckListOption saveCheckListOption(CheckListOption option) {
        return checkListOptionRepository.save(option);
    }

    /**
     * CheckListOptionFile 엔티티를 저장한다.
     */
    public CheckListOptionFile saveCheckListOptionFile(CheckListOptionFile file) {
        return checkListOptionFileRepository.save(file);
    }

    /**
     * CheckListItem 스냅샷으로 변환 후 히스토리 엔티티에 저장
     * @param checkListItem CheckListItem 엔티티
     * @param checkListItemId CheckListItem 식별자
     * @param actionType 액션 타입
     */
    public void snapShotAndRecordHistory(CheckListItem checkListItem, Long checkListItemId, ActionType actionType) {
        CheckListItemHistorySnapShot snapshot = CheckListItemHistorySnapShot.from(checkListItem);
        historyRecorder.recordHistory(HistoryType.CHECK_LIST_ITEM, checkListItemId, actionType, snapshot);
    }

    /**
     * CheckListItemComment 스냅샷으로 변환 후 히스토리 엔티티에 저장
     * @param comment CheckListItemComment 엔티티
     * @param commentId Comment 식별자
     * @param actionType 액션 타입
     */
    public void snapShotAndRecordHistory(CheckListItemComment comment, Long commentId, ActionType actionType) {
        CheckListItemCommentHistorySnapShot snapshot = CheckListItemCommentHistorySnapShot.from(comment);
        historyRecorder.recordHistory(HistoryType.CHECK_LIST_ITEM_COMMENT, commentId, actionType, snapshot);
    }

    public void existNodeCheck(Long nodeId) {
        if (checkListRepository.findByProjectNodeId(nodeId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS_CHECK_LIST);
        }
    }
}
