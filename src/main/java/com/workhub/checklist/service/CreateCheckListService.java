package com.workhub.checklist.service;

import com.workhub.checklist.dto.*;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListOption;
import com.workhub.checklist.entity.CheckListOptionFile;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateCheckListService {

    private final CheckListService checkListService;
    private final CheckListAccessValidator checkListAccessValidator;

    /**
     * CheckList를 생성한다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param userId 사용자 식별자
     * @param request CheckList 생성 요청
     * @return CheckListResponse
     */
    public CheckListResponse create(Long projectId, Long nodeId, Long userId, CheckListCreateRequest request) {

        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectDevMember(projectId);

        validateItemOrders(request.items());
        checkListService.existNodeCheck(nodeId);

        CheckList checkList = createCheckList(request, userId, nodeId);
        List<CheckListItemResponse> itemResponses = createCheckListItems(checkList.getCheckListId(), request.items(), userId);

        return CheckListResponse.from(checkList, itemResponses);
    }

    /**
     * CheckList 엔티티를 생성하고 저장한다.
     * @param request CheckList 생성 요청
     * @param userId 사용자 식별자
     * @return 저장된 CheckList 엔티티
     */
    private CheckList createCheckList(CheckListCreateRequest request, Long userId, Long nodeId) {
        CheckList checkList = CheckList.of(request, userId, nodeId);
        return checkListService.saveCheckList(checkList);
    }

    /**
     * CheckListItem 목록을 생성하고 히스토리를 기록한다.
     * @param checkListId CheckList 식별자
     * @param itemRequests CheckListItem 생성 요청 목록
     * @param userId 사용자 식별자
     * @return CheckListItemResponse 목록
     */
    private List<CheckListItemResponse> createCheckListItems(Long checkListId, List<CheckListItemRequest> itemRequests, Long userId) {

        return itemRequests.stream().map(itemRequest ->
                createCheckListItem(checkListId, itemRequest, userId))
                .collect(Collectors.toList());
    }

    /**
     * 단일 CheckListItem을 생성하고 히스토리를 기록한다.
     * @param checkListId CheckList 식별자
     * @param itemRequest CheckListItem 생성 요청
     * @param userId 사용자 식별자
     * @return CheckListItemResponse
     */
    private CheckListItemResponse createCheckListItem(Long checkListId, CheckListItemRequest itemRequest, Long userId) {
        // Item 생성 및 저장
        CheckListItem item = CheckListItem.of(checkListId, itemRequest, userId);
        item = checkListService.saveCheckListItem(item);

        // 히스토리 기록
        checkListService.snapShotAndRecordHistory(item, item.getCheckListItemId(), ActionType.CREATE);

        // Option 목록 생성
        List<CheckListOptionResponse> optionResponses = createCheckListOptions(item.getCheckListItemId(), itemRequest.options());

        return CheckListItemResponse.from(item, optionResponses);
    }

    /**
     * CheckListOption 목록을 생성한다.
     * @param itemId CheckListItem 식별자
     * @param optionRequests CheckListOption 생성 요청 목록
     * @return CheckListOptionResponse 목록
     */
    private List<CheckListOptionResponse> createCheckListOptions(Long itemId, List<CheckListOptionRequest> optionRequests) {

        return optionRequests.stream().map(optionRequest ->
                createCheckListOption(itemId, optionRequest))
                .collect(Collectors.toList());
    }

    /**
     * 단일 CheckListOption을 생성한다.
     * @param itemId CheckListItem 식별자
     * @param optionRequest CheckListOption 생성 요청
     * @return CheckListOptionResponse
     */
    private CheckListOptionResponse createCheckListOption(Long itemId, CheckListOptionRequest optionRequest) {
        CheckListOption option = CheckListOption.of(itemId, optionRequest);
        checkListService.saveCheckListOption(option);

        List<CheckListOptionFileResponse> fileResponses = createCheckListOptionFiles(option.getCheckListOptionId(), optionRequest.fileUrls());

        return CheckListOptionResponse.from(option, fileResponses);
    }

    /**
     * CheckListOptionFile 목록을 생성한다.
     * @param optionId CheckListOption 식별자
     * @param fileUrls 파일 URL 목록
     * @return CheckListOptionFileResponse 목록
     */
    private List<CheckListOptionFileResponse> createCheckListOptionFiles(Long optionId, List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return new ArrayList<>();
        }

        List<CheckListOptionFileResponse> fileResponses = new ArrayList<>();

        for (int i = 0; i < fileUrls.size(); i++) {
            String fileUrl = fileUrls.get(i);
            CheckListOptionFile file = CheckListOptionFile.of(optionId, fileUrl, i);
            checkListService.saveCheckListOptionFile(file);
            fileResponses.add(CheckListOptionFileResponse.from(file));
        }

        return fileResponses;
    }

    private void validateItemOrders(List<CheckListItemRequest> itemRequests) {
        if (itemRequests == null || itemRequests.isEmpty()) {
            return;
        }

        Set<Integer> itemOrders = new HashSet<>();
        for (CheckListItemRequest itemRequest : itemRequests) {
            if (!itemOrders.add(itemRequest.itemOrder())) {
                throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_ITEM_ORDER);
            }
            validateOptionOrders(itemRequest.options());
        }
    }

    private void validateOptionOrders(List<CheckListOptionRequest> optionRequests) {
        if (optionRequests == null || optionRequests.isEmpty()) {
            return;
        }

        Set<Integer> optionOrders = new HashSet<>();
        for (CheckListOptionRequest optionRequest : optionRequests) {
            if (!optionOrders.add(optionRequest.optionOrder())) {
                throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_OPTION_ORDER);
            }
        }
    }
}
