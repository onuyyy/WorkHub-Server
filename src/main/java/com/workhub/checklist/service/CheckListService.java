package com.workhub.checklist.service;

import com.workhub.checklist.dto.CheckListDetails;
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

import java.util.List;

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
     * nodeId로 CheckList를 조회한다. (노드별 체크리스트는 1개이기 때문에)
     */
    public CheckList findByNodeId(Long nodeId) {
        return checkListRepository.findByProjectNodeId(nodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST));
    }

    /**
     * CheckListItem 엔티티를 저장한다.
     */
    public CheckListItem saveCheckListItem(CheckListItem item) {
        return checkListItemRepository.save(item);
    }

    public CheckListItem findCheckListItem(Long itemId) {
        return checkListItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECK_LIST_ITEM_NOT_FOUND));
    }

    /**
     * CheckListOption 엔티티를 저장한다.
     */
    public CheckListOption saveCheckListOption(CheckListOption option) {
        return checkListOptionRepository.save(option);
    }

    public CheckListOption findCheckListOption(Long optionId) {
        return checkListOptionRepository.findById(optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECK_LIST_OPTION_NOT_FOUND));
    }

    /**
     * CheckListOptionFile 엔티티를 저장한다.
     */
    public CheckListOptionFile saveCheckListOptionFile(CheckListOptionFile file) {
        return checkListOptionFileRepository.save(file);
    }

    public CheckListOptionFile findCheckListOptionFile(Long fileId) {
        return checkListOptionFileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHECK_LIST_OPTION_FILE_NOT_FOUND));
    }

    /**
     * CheckList에 속한 Item 목록을 조회한다.
     */
    public List<CheckListItem> findItemsByCheckListId(Long checkListId) {
        return checkListItemRepository.findAllByCheckListIdOrderByItemOrderAsc(checkListId);
    }

    /**
     * CheckListItem에 속한 Option 목록을 조회한다.
     */
    public List<CheckListOption> findOptionsByCheckListItemId(Long checkListItemId) {
        return checkListOptionRepository.findAllByCheckListItemIdOrderByOptionOrderAsc(checkListItemId);
    }

    /**
     * CheckListOption에 속한 파일 목록을 조회한다.
     */
    public List<CheckListOptionFile> findOptionFilesByCheckListOptionId(Long optionId) {
        return checkListOptionFileRepository.findAllByCheckListOptionIdOrderByFileOrderAsc(optionId);
    }

    /**
     * 전달된 파일 엔티티들을 삭제한다.
     */
    public void deleteCheckListOptionFiles(List<CheckListOptionFile> files) {
        checkListOptionFileRepository.deleteAll(files);
    }

    public void deleteCheckListOptionFile(CheckListOptionFile file) {
        checkListOptionFileRepository.delete(file);
    }

    /**
     * 전달된 옵션 엔티티들을 삭제한다.
     */
    public void deleteCheckListOptions(List<CheckListOption> options) {
        checkListOptionRepository.deleteAll(options);
    }

    public void deleteCheckListOption(CheckListOption option) {
        checkListOptionRepository.delete(option);
    }

    /**
     * 전달된 아이템 엔티티들을 삭제한다.
     */
    public void deleteCheckListItems(List<CheckListItem> items) {
        checkListItemRepository.deleteAll(items);
    }

    public void deleteCheckListItem(CheckListItem item) {
        checkListItemRepository.delete(item);
    }

    public void existNodeCheck(Long nodeId) {
        if (checkListRepository.findByProjectNodeId(nodeId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS_CHECK_LIST);
        }
    }

    /**
     * CheckList의 전체 계층 구조를 효율적으로 조회한다.
     *
     * @param checkListId 체크리스트 ID
     * @return 체크리스트 상세 정보
     */
    public CheckListDetails findCheckListDetailsById(Long checkListId) {
        return checkListRepository.findCheckListDetailsById(checkListId);
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
}
