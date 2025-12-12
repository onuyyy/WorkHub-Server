package com.workhub.checklist.service;

import com.workhub.checklist.dto.*;
import com.workhub.checklist.entity.CheckList;
import com.workhub.checklist.entity.CheckListItem;
import com.workhub.checklist.entity.CheckListOption;
import com.workhub.checklist.entity.CheckListOptionFile;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 체크리스트 업데이트 서비스
 * - 부분 업데이트 처리 방식
 * 클라이언트는 변경된 항목만 전송하며, 서버는 다음과 같이 처리합니다:
 *   요청에 포함된 항목: changeType(CREATE/UPDATE/DELETE)에 따라 처리
 *   요청에 없는 기존 항목: 그대로 유지 (변경 없음)
 *
 * - 처리 흐름
 * 1. 변경된 항목만 CheckListUpdateRequest.items에 포함하여 전송
 * 2. 각 항목의 changeType 검증 (validate())
 * 3. changeType에 따라 CREATE/UPDATE/DELETE 수행
 * 4. UPDATE 시 null 필드는 기존 값 유지
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCheckListService {

    private final CheckListService checkListService;
    private final CheckListAccessValidator checkListAccessValidator;

    /**
     * 체크리스트 업데이트
     *
     * 부분 업데이트 지원: 변경된 항목만 request.items에 포함
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param request 업데이트 요청 (변경된 항목만 포함)
     * @return 업데이트된 체크리스트 응답
     */
    public CheckListResponse update(Long projectId, Long nodeId, CheckListUpdateRequest request) {

        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectDevMemberOrAdmin(projectId);

        CheckList checkList = checkListService.findByNodeId(nodeId);

        updateDescriptionIfNeeded(checkList, request.description());

        Long currentUserId = SecurityUtil.getCurrentUserIdOrThrow();

        if (request.items() != null) {
            request.items().forEach(itemRequest ->
                    handleItemChange(checkList, itemRequest, currentUserId));
        }

        CheckListDetails details = checkListService.findCheckListDetailsById(checkList.getCheckListId());
        return checkListService.buildResponse(details);
    }

    /**
     * 체크리스트 설명 업데이트 (null이 아닌 경우에만)
     *
     * @param checkList 체크리스트 엔티티
     * @param description 새로운 설명 (null이면 변경하지 않음)
     */
    private void updateDescriptionIfNeeded(CheckList checkList, String description) {
        if (description != null) {
            checkList.updateDescription(description);
        }
    }

    /**
     * 체크리스트 항목 변경 처리 (생성/수정/삭제)
     *
     * @param checkList 체크리스트 엔티티
     * @param request 항목 변경 요청
     * @param userId 현재 사용자 ID
     */
    private void handleItemChange(CheckList checkList,
                                  CheckListItemUpdateRequest request,
                                  Long userId) {

        // DTO 검증 (changeType에 따른 필수 필드 체크)
        request.validate();

        CheckListUpdateCommandType commandType = request.changeType();

        switch (commandType) {
            case CREATE -> createItem(checkList, request, userId);
            case UPDATE -> updateItem(checkList, request);
            case DELETE -> deleteItem(checkList, request);
            default -> throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }

    /**
     * 체크리스트 항목 생성
     *
     * @param checkList 체크리스트 엔티티
     * @param request 항목 생성 요청
     * @param userId 생성자 ID
     */
    private void createItem(CheckList checkList,
                            CheckListItemUpdateRequest request,
                            Long userId) {
        // validate()에서 이미 검증됨
        CheckListItem item = CheckListItem.of(
                checkList.getCheckListId(),
                request.itemTitle(),
                request.itemOrder(),
                request.templateId(),
                userId
        );

        item = checkListService.saveCheckListItem(item);
        checkListService.snapShotAndRecordHistory(item, item.getCheckListItemId(), ActionType.CREATE);

        processOptionRequests(item.getCheckListItemId(), request.options());
    }

    /**
     * 체크리스트 항목 수정 (부분 업데이트)
     * null 필드는 기존 값 유지
     *
     * @param checkList 체크리스트 엔티티
     * @param request 항목 수정 요청 (변경할 필드만 포함)
     */
    private void updateItem(CheckList checkList, CheckListItemUpdateRequest request) {
        Long itemId = requireItemId(request);
        CheckListItem item = checkListService.findCheckListItem(itemId);
        validateItemBelongsToCheckList(item, checkList.getCheckListId());

        checkListService.snapShotAndRecordHistory(item, itemId, ActionType.UPDATE);
        item.updateItem(request.itemTitle(), request.itemOrder(), request.templateId());

        processOptionRequests(itemId, request.options());
    }

    /**
     * 체크리스트 항목 삭제 (연관된 선택지와 파일도 함께 삭제)
     *
     * @param checkList 체크리스트 엔티티
     * @param request 항목 삭제 요청
     */
    private void deleteItem(CheckList checkList, CheckListItemUpdateRequest request) {
        Long itemId = requireItemId(request);
        CheckListItem item = checkListService.findCheckListItem(itemId);
        validateItemBelongsToCheckList(item, checkList.getCheckListId());

        deleteOptionsWithFiles(itemId);

        checkListService.snapShotAndRecordHistory(item, itemId, ActionType.DELETE);
        checkListService.deleteCheckListItem(item);
    }

    /**
     * 선택지 변경 요청 일괄 처리
     *
     * @param itemId 항목 ID
     * @param optionRequests 선택지 변경 요청 리스트
     */
    private void processOptionRequests(Long itemId, List<CheckListOptionUpdateRequest> optionRequests) {
        if (optionRequests == null) {
            return;
        }

        optionRequests.forEach(optionRequest ->
                handleOptionChange(itemId, optionRequest));
    }

    /**
     * 선택지 변경 처리 (생성/수정/삭제)
     *
     * @param itemId 항목 ID
     * @param request 선택지 변경 요청
     */
    private void handleOptionChange(Long itemId, CheckListOptionUpdateRequest request) {
        if (request == null) {
            return;
        }

        // DTO 검증 (이미 item validate에서 호출되었지만, 명시적 검증)
        request.validate();

        CheckListUpdateCommandType commandType = request.changeType();

        switch (commandType) {
            case CREATE -> createOption(itemId, request);
            case UPDATE -> updateOption(itemId, request);
            case DELETE -> deleteOption(itemId, request);
            default -> throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }

    /**
     * 선택지 생성
     *
     * @param itemId 항목 ID
     * @param request 선택지 생성 요청
     */
    private void createOption(Long itemId, CheckListOptionUpdateRequest request) {
        // validate()에서 이미 검증됨
        CheckListOption option = CheckListOption.of(
                itemId,
                request.optionContent(),
                request.optionOrder()
        );

        option = checkListService.saveCheckListOption(option);
        processFileRequests(option.getCheckListOptionId(), request.files());
    }

    /**
     * 선택지 수정 (부분 업데이트)
     * null 필드는 기존 값 유지
     *
     * @param itemId 항목 ID
     * @param request 선택지 수정 요청 (변경할 필드만 포함)
     */
    private void updateOption(Long itemId, CheckListOptionUpdateRequest request) {
        Long optionId = requireOptionId(request);
        CheckListOption option = checkListService.findCheckListOption(optionId);
        validateOptionBelongs(option, itemId);

        option.updateOption(request.optionContent(), request.optionOrder());
        processFileRequests(option.getCheckListOptionId(), request.files());
    }

    /**
     * 선택지 삭제 (연관된 파일도 함께 삭제)
     *
     * @param itemId 항목 ID
     * @param request 선택지 삭제 요청
     */
    private void deleteOption(Long itemId, CheckListOptionUpdateRequest request) {
        Long optionId = requireOptionId(request);
        CheckListOption option = checkListService.findCheckListOption(optionId);
        validateOptionBelongs(option, itemId);

        List<CheckListOptionFile> files =
                checkListService.findOptionFilesByCheckListOptionId(optionId);
        if (!files.isEmpty()) {
            checkListService.deleteCheckListOptionFiles(files);
        }
        checkListService.deleteCheckListOption(option);
    }

    /**
     * 파일 변경 요청 일괄 처리
     *
     * @param optionId 선택지 ID
     * @param fileRequests 파일 변경 요청 리스트
     */
    private void processFileRequests(Long optionId,
                                     List<CheckListOptionFileUpdateRequest> fileRequests) {
        if (fileRequests == null) {
            return;
        }

        fileRequests.forEach(fileRequest -> handleFileChange(optionId, fileRequest));
    }

    /**
     * 파일 변경 처리 (생성/수정/삭제)
     *
     * @param optionId 선택지 ID
     * @param fileRequest 파일 변경 요청
     */
    private void handleFileChange(Long optionId, CheckListOptionFileUpdateRequest fileRequest) {
        if (fileRequest == null) {
            return;
        }

        // DTO 검증 (이미 option validate에서 호출되었지만, 명시적 검증)
        fileRequest.validate();

        CheckListUpdateCommandType commandType = fileRequest.changeType();

        switch (commandType) {
            case CREATE -> createFile(optionId, fileRequest);
            case UPDATE -> updateFile(optionId, fileRequest);
            case DELETE -> deleteFile(optionId, fileRequest);
            default -> throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }

    /**
     * 파일 생성
     *
     * @param optionId 선택지 ID
     * @param fileRequest 파일 생성 요청
     */
    private void createFile(Long optionId, CheckListOptionFileUpdateRequest fileRequest) {
        // validate()에서 이미 검증됨
        CheckListOptionFile file = CheckListOptionFile.of(optionId,
                fileRequest.fileUrl(), fileRequest.fileOrder());
        checkListService.saveCheckListOptionFile(file);
    }

    /**
     * 파일 수정 (부분 업데이트)
     *
     * null 필드는 기존 값 유지
     *
     * @param optionId 선택지 ID
     * @param fileRequest 파일 수정 요청 (변경할 필드만 포함)
     */
    private void updateFile(Long optionId, CheckListOptionFileUpdateRequest fileRequest) {
        Long fileId = requireFileId(fileRequest);
        CheckListOptionFile file = checkListService.findCheckListOptionFile(fileId);
        validateFileBelongs(file, optionId);
        file.updateFile(fileRequest.fileUrl(), fileRequest.fileOrder());
    }

    /**
     * 파일 삭제
     *
     * @param optionId 선택지 ID
     * @param fileRequest 파일 삭제 요청
     */
    private void deleteFile(Long optionId, CheckListOptionFileUpdateRequest fileRequest) {
        Long fileId = requireFileId(fileRequest);
        CheckListOptionFile file = checkListService.findCheckListOptionFile(fileId);
        validateFileBelongs(file, optionId);
        checkListService.deleteCheckListOptionFile(file);
    }

    /**
     * 항목에 속한 모든 선택지와 파일 삭제
     *
     * @param itemId 항목 ID
     */
    private void deleteOptionsWithFiles(Long itemId) {
        List<CheckListOption> options = checkListService.findOptionsByCheckListItemId(itemId);
        for (CheckListOption option : options) {
            List<CheckListOptionFile> files =
                    checkListService.findOptionFilesByCheckListOptionId(option.getCheckListOptionId());
            if (!files.isEmpty()) {
                checkListService.deleteCheckListOptionFiles(files);
            }
        }

        if (!options.isEmpty()) {
            checkListService.deleteCheckListOptions(options);
        }
    }

    /**
     * 체크리스트 아이템의 상태(동의/보류) 를 수정
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param checkListId 체크리스트 식별자
     * @param checkListItemId 체크리스트 아이템 식별자
     * @param status 상태 값
     * @return CheckListItemStatus 변경된 상태 값
     */
    public CheckListItemStatus updateStatus(
            Long projectId,
            Long nodeId,
            Long checkListId,
            Long checkListItemId,
            CheckListItemStatus status
    ) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.chekProjectClientMember(projectId);

        CheckList checkList = checkListService.findById(checkListId);
        CheckListItem item = checkListService.findCheckListItem(checkListItemId);
        validateItemBelongsToCheckList(item, checkList.getCheckListId());

        item.updateStatus(status);
        checkListService.snapShotAndRecordHistory(item, item.getCheckListItemId(), ActionType.UPDATE);

        return item.getStatus();
    }

    private Long requireItemId(CheckListItemUpdateRequest request) {
        if (request.checkListItemId() == null) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
        return request.checkListItemId();
    }

    private Long requireOptionId(CheckListOptionUpdateRequest request) {
        if (request.checkListOptionId() == null) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
        return request.checkListOptionId();
    }

    private Long requireFileId(CheckListOptionFileUpdateRequest request) {
        if (request.checkListOptionFileId() == null) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
        return request.checkListOptionFileId();
    }

    private void validateItemBelongsToCheckList(CheckListItem item, Long checkListId) {
        if (!Objects.equals(item.getCheckListId(), checkListId)) {
            throw new BusinessException(ErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST);
        }
    }

    private void validateOptionBelongs(CheckListOption option, Long itemId) {
        if (!Objects.equals(option.getCheckListItemId(), itemId)) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }

    private void validateFileBelongs(CheckListOptionFile file, Long optionId) {
        if (!Objects.equals(file.getCheckListOptionId(), optionId)) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }
}
