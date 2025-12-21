package com.workhub.checklist.service.checkList;

import com.workhub.checklist.dto.checkList.*;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.checkList.CheckListOption;
import com.workhub.checklist.entity.checkList.CheckListOptionFile;
import com.workhub.checklist.event.CheckListItemStatusChangedEvent;
import com.workhub.checklist.event.CheckListUpdatedEvent;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCheckListService {

    private final CheckListService checkListService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 체크리스트 업데이트
     *
     * 부분 업데이트 지원: 변경된 항목만 request.items에 포함
     *
     * @param projectId 프로젝트 ID
     * @param nodeId 노드 ID
     * @param request 업데이트 요청 (변경된 항목만 포함)
     * @param newFiles 신규 업로드 파일
     * @return 업데이트된 체크리스트 응답
     */
    public CheckListResponse update(Long projectId,
                                    Long nodeId,
                                    CheckListUpdateRequest request,
                                    List<MultipartFile> newFiles) {

        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectDevMemberOrAdmin(projectId);

        CheckList checkList = checkListService.findByNodeId(nodeId);

        updateDescriptionIfNeeded(checkList, request.description());

        Long currentUserId = SecurityUtil.getCurrentUserIdOrThrow();

        List<FileUploadResponse> uploadResponses = fileService.uploadFiles(newFiles);
        CheckListFileUploadContext uploadContext = new CheckListFileUploadContext(uploadResponses);
        List<String> uploadedFileNames = uploadContext.getUploadedFileNames();
        List<String> filesToDelete = new ArrayList<>();

        try {
            if (request.items() != null) {
                request.items().forEach(itemRequest ->
                        handleItemChange(checkList, itemRequest, currentUserId, uploadContext, filesToDelete));
            }

            if (uploadContext.hasUnconsumedFiles()) {
                List<String> unconsumed = uploadContext.getUnconsumedFileNames();
                log.error("체크리스트 수정 실패 - 미사용 파일 {} 개: {}", unconsumed.size(), unconsumed);
                throw new BusinessException(ErrorCode.CHECK_LIST_FILE_MAPPING_NOT_FOUND);
            }

            CheckListDetails details = checkListService.findCheckListDetailsById(checkList.getCheckListId());

            deleteFilesFromS3(filesToDelete);

            eventPublisher.publishEvent(new CheckListUpdatedEvent(
                    projectId,
                    nodeId,
                    "체크리스트가 수정되었습니다."
            ));
          
            return checkListService.buildResponse(
                    details,
                    checkListService.resolveUserInfo(checkList.getUserId())
            );
        } catch (Exception e) {
            rollbackUploadedFiles(uploadedFileNames);
            throw e;
        }
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
                                  Long userId,
                                  CheckListFileUploadContext uploadContext,
                                  List<String> filesToDelete) {

        request.validate();

        CheckListUpdateCommandType commandType = request.changeType();

        switch (commandType) {
            case CREATE -> createItem(checkList, request, userId, uploadContext, filesToDelete);
            case UPDATE -> updateItem(checkList, request, uploadContext, filesToDelete);
            case DELETE -> deleteItem(checkList, request, filesToDelete);
            default -> throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }

    /**
     * 체크리스트 항목 생성
     */
    private void createItem(CheckList checkList,
                            CheckListItemUpdateRequest request,
                            Long userId,
                            CheckListFileUploadContext uploadContext,
                            List<String> filesToDelete) {
        CheckListItem item = CheckListItem.of(
                checkList.getCheckListId(),
                request.itemTitle(),
                request.itemOrder(),
                request.templateId(),
                userId
        );

        item = checkListService.saveCheckListItem(item);
        checkListService.snapShotAndRecordHistory(item, item.getCheckListItemId(), ActionType.CREATE);

        processOptionRequests(item.getCheckListItemId(), request.options(), uploadContext, filesToDelete);
    }

    /**
     * 체크리스트 항목 수정 (부분 업데이트)
     */
    private void updateItem(CheckList checkList,
                            CheckListItemUpdateRequest request,
                            CheckListFileUploadContext uploadContext,
                            List<String> filesToDelete) {
        Long itemId = requireItemId(request);
        CheckListItem item = checkListService.findCheckListItem(itemId);
        validateItemBelongsToCheckList(item, checkList.getCheckListId());

        checkListService.snapShotAndRecordHistory(item, itemId, ActionType.UPDATE);
        item.updateItem(request.itemTitle(), request.itemOrder(), request.templateId());

        processOptionRequests(itemId, request.options(), uploadContext, filesToDelete);
    }

    /**
     * 체크리스트 항목 삭제 (연관된 선택지와 파일도 함께 삭제)
     */
    private void deleteItem(CheckList checkList,
                            CheckListItemUpdateRequest request,
                            List<String> filesToDelete) {
        Long itemId = requireItemId(request);
        CheckListItem item = checkListService.findCheckListItem(itemId);
        validateItemBelongsToCheckList(item, checkList.getCheckListId());

        deleteOptionsWithFiles(itemId, filesToDelete);

        checkListService.snapShotAndRecordHistory(item, itemId, ActionType.DELETE);
        checkListService.deleteCheckListItem(item);
    }

    /**
     * 선택지 변경 요청 일괄 처리
     */
    private void processOptionRequests(Long itemId,
                                       List<CheckListOptionUpdateRequest> optionRequests,
                                       CheckListFileUploadContext uploadContext,
                                       List<String> filesToDelete) {
        if (optionRequests == null) {
            return;
        }

        optionRequests.forEach(optionRequest ->
                handleOptionChange(itemId, optionRequest, uploadContext, filesToDelete));
    }

    /**
     * 선택지 변경 처리 (생성/수정/삭제)
     */
    private void handleOptionChange(Long itemId,
                                    CheckListOptionUpdateRequest request,
                                    CheckListFileUploadContext uploadContext,
                                    List<String> filesToDelete) {
        if (request == null) {
            return;
        }

        request.validate();

        CheckListUpdateCommandType commandType = request.changeType();

        switch (commandType) {
            case CREATE -> createOption(itemId, request, uploadContext, filesToDelete);
            case UPDATE -> updateOption(itemId, request, uploadContext, filesToDelete);
            case DELETE -> deleteOption(itemId, request, filesToDelete);
            default -> throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }

    /**
     * 선택지 생성
     */
    private void createOption(Long itemId,
                              CheckListOptionUpdateRequest request,
                              CheckListFileUploadContext uploadContext,
                              List<String> filesToDelete) {
        CheckListOption option = CheckListOption.of(
                itemId,
                request.optionContent(),
                request.optionOrder()
        );

        option = checkListService.saveCheckListOption(option);
        processFileRequests(option.getCheckListOptionId(), request.files(), uploadContext, filesToDelete);
    }

    /**
     * 선택지 수정 (부분 업데이트)
     */
    private void updateOption(Long itemId,
                              CheckListOptionUpdateRequest request,
                              CheckListFileUploadContext uploadContext,
                              List<String> filesToDelete) {
        Long optionId = requireOptionId(request);
        CheckListOption option = checkListService.findCheckListOption(optionId);
        validateOptionBelongs(option, itemId);

        option.updateOption(request.optionContent(), request.optionOrder());
        processFileRequests(option.getCheckListOptionId(), request.files(), uploadContext, filesToDelete);
    }

    /**
     * 선택지 삭제 (연관된 파일도 함께 삭제)
     */
    private void deleteOption(Long itemId,
                              CheckListOptionUpdateRequest request,
                              List<String> filesToDelete) {
        Long optionId = requireOptionId(request);
        CheckListOption option = checkListService.findCheckListOption(optionId);
        validateOptionBelongs(option, itemId);

        List<CheckListOptionFile> files =
                checkListService.findOptionFilesByCheckListOptionId(optionId);
        if (!files.isEmpty()) {
            files.forEach(file -> trackDeletionIfManaged(file, filesToDelete));
            checkListService.deleteCheckListOptionFiles(files);
        }
        checkListService.deleteCheckListOption(option);
    }

    /**
     * 파일 변경 요청 일괄 처리
     */
    private void processFileRequests(Long optionId,
                                     List<CheckListOptionFileUpdateRequest> fileRequests,
                                     CheckListFileUploadContext uploadContext,
                                     List<String> filesToDelete) {
        if (fileRequests == null) {
            return;
        }

        fileRequests.forEach(fileRequest ->
                handleFileChange(optionId, fileRequest, uploadContext, filesToDelete));
    }

    /**
     * 파일 변경 처리 (생성/수정/삭제)
     */
    private void handleFileChange(Long optionId,
                                  CheckListOptionFileUpdateRequest fileRequest,
                                  CheckListFileUploadContext uploadContext,
                                  List<String> filesToDelete) {
        if (fileRequest == null) {
            return;
        }

        fileRequest.validate();

        CheckListUpdateCommandType commandType = fileRequest.changeType();

        switch (commandType) {
            case CREATE -> createFile(optionId, fileRequest, uploadContext);
            case UPDATE -> updateFile(optionId, fileRequest);
            case DELETE -> deleteFile(optionId, fileRequest, filesToDelete);
            default -> throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_UPDATE_COMMAND);
        }
    }

    /**
     * 파일 생성
     */
    private void createFile(Long optionId,
                            CheckListOptionFileUpdateRequest fileRequest,
                            CheckListFileUploadContext uploadContext) {
        CheckListOptionFile file;

        // 1순위: URL 형식 체크 (기존 S3 파일 또는 외부 링크)
        if (isValidRemoteUrl(fileRequest.fileUrl())) {
            String fileUrl = fileRequest.fileUrl();

            // S3 URL인 경우 UUID 파일명만 추출
            if (isS3Url(fileUrl)) {
                fileUrl = extractFileNameFromUrl(fileUrl);
            }
            // 외부 링크는 전체 URL 유지

            file = CheckListOptionFile.of(optionId, fileUrl, fileRequest.fileOrder());
        }
        // 2순위: 새로 업로드된 파일 체크
        else {
            Optional<FileUploadResponse> upload = uploadContext.consume(fileRequest.fileUrl());
            if (upload.isPresent()) {
                file = CheckListOptionFile.fromUpload(optionId, upload.get(), fileRequest.fileOrder());
            } else {
                String availableFiles = String.join(", ", uploadContext.getUploadedFileNames());

                log.error("파일 매핑 실패 - 요청 파일: '{}', 업로드된 파일: [{}]",
                        fileRequest.fileUrl(), availableFiles);

                throw new BusinessException(ErrorCode.CHECK_LIST_FILE_MAPPING_NOT_FOUND);
            }
        }

        checkListService.saveCheckListOptionFile(file);
    }

    /**
     * 파일 수정 (부분 업데이트)
     */
    private void updateFile(Long optionId, CheckListOptionFileUpdateRequest fileRequest) {
        Long fileId = requireFileId(fileRequest);
        CheckListOptionFile file = checkListService.findCheckListOptionFile(fileId);
        validateFileBelongs(file, optionId);
        file.updateFile(fileRequest.fileUrl(), fileRequest.fileOrder());
    }

    /**
     * 파일 삭제
     */
    private void deleteFile(Long optionId,
                            CheckListOptionFileUpdateRequest fileRequest,
                            List<String> filesToDelete) {
        Long fileId = requireFileId(fileRequest);
        CheckListOptionFile file = checkListService.findCheckListOptionFile(fileId);
        validateFileBelongs(file, optionId);
        trackDeletionIfManaged(file, filesToDelete);
        checkListService.deleteCheckListOptionFile(file);
    }

    /**
     * 항목에 속한 모든 선택지와 파일 삭제
     */
    private void deleteOptionsWithFiles(Long itemId, List<String> filesToDelete) {
        List<CheckListOption> options = checkListService.findOptionsByCheckListItemId(itemId);
        for (CheckListOption option : options) {
            List<CheckListOptionFile> files =
                    checkListService.findOptionFilesByCheckListOptionId(option.getCheckListOptionId());
            if (!files.isEmpty()) {
                files.forEach(file -> trackDeletionIfManaged(file, filesToDelete));
                checkListService.deleteCheckListOptionFiles(files);
            }
        }

        if (!options.isEmpty()) {
            checkListService.deleteCheckListOptions(options);
        }
    }

    /**
     * 체크리스트 아이템의 상태(동의/보류) 를 수정
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

        eventPublisher.publishEvent(new CheckListItemStatusChangedEvent(
                projectId,
                nodeId,
                checkListId,
                checkListItemId,
                item.getItemTitle(),
                status
        ));

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
            throw new BusinessException(ErrorCode.CHECK_LIST_OPTION_NOT_FOUND);
        }
    }

    private void validateFileBelongs(CheckListOptionFile file, Long optionId) {
        if (!Objects.equals(file.getCheckListOptionId(), optionId)) {
            throw new BusinessException(ErrorCode.CHECK_LIST_OPTION_FILE_NOT_FOUND);
        }
    }

    private boolean isValidRemoteUrl(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return false;
        }

        // http:// 또는 https://로 시작하는 문자열은 외부 링크로 간주
        return identifier.startsWith("http://") || identifier.startsWith("https://");
    }

    /**
     * S3 URL에서 파일명만 추출 (UUID 파일명)
     * 예: https://s3.../abc-123.pdf → abc-123.pdf
     */
    private String extractFileNameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }

        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < url.length() - 1) {
            return url.substring(lastSlashIndex + 1);
        }

        return url;
    }

    /**
     * S3 도메인 URL인지 확인
     */
    private boolean isS3Url(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        return url.contains(".s3.") || url.contains("s3.amazonaws.com");
    }

    private void trackDeletionIfManaged(CheckListOptionFile file, List<String> filesToDelete) {
        if (file != null && file.isManagedFile()) {
            filesToDelete.add(file.getFileUrl());
        }
    }

    private void rollbackUploadedFiles(List<String> uploadedFileNames) {
        if (uploadedFileNames == null || uploadedFileNames.isEmpty()) {
            return;
        }

        try {
            log.warn("체크리스트 수정 실패로 업로드된 파일 {}개를 삭제합니다.", uploadedFileNames.size());
            fileService.deleteFiles(uploadedFileNames);
        } catch (Exception deleteException) {
            log.error("체크리스트 수정 실패 - 업로드 파일 삭제 중 오류 발생: {}", uploadedFileNames, deleteException);
        }
    }

    private void deleteFilesFromS3(List<String> filesToDelete) {
        if (filesToDelete == null || filesToDelete.isEmpty()) {
            return;
        }

        try {
            fileService.deleteFiles(filesToDelete);
        } catch (Exception deleteException) {
            log.error("체크리스트 파일 삭제 중 오류 발생: {}", filesToDelete, deleteException);
        }
    }
}
