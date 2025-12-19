package com.workhub.checklist.service.checkList;

import com.workhub.checklist.dto.checkList.*;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.checkList.CheckListOption;
import com.workhub.checklist.entity.checkList.CheckListOptionFile;
import com.workhub.checklist.event.CheckListCreatedEvent;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateCheckListService {

    private final CheckListService checkListService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final FileService fileService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * CheckList를 생성한다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param userId 사용자 식별자
     * @param request CheckList 생성 요청
     * @return CheckListResponse
     */
    public CheckListResponse create(Long projectId,
                                    Long nodeId,
                                    Long userId,
                                    CheckListCreateRequest request,
                                    List<MultipartFile> files) {

        // 1) 권한 및 유효성 검증
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectDevMemberOrAdmin(projectId);
        validateItemOrders(request.items());
        checkListService.existNodeCheck(nodeId);

        // 2) S3에 파일 업로드
        List<FileUploadResponse> uploadResponses = fileService.uploadFiles(files);

        // 3) 업로드 컨텍스트 생성
        CheckListFileUploadContext uploadContext = new CheckListFileUploadContext(uploadResponses);
        List<String> uploadedFileNames = uploadContext.getUploadedFileNames();

        try {
            // 4) CheckList 엔티티 생성
            CheckList checkList = createCheckList(request, userId, nodeId);
            // CheckListItem 및 하위 엔티티 생성
            List<CheckListItemResponse> itemResponses =
                    createCheckListItems(checkList.getCheckListId(), request.items(), userId, uploadContext);

            // 6) 미소비 파일 검증
            if (uploadContext.hasUnconsumedFiles()) {
                List<String> unconsumed = uploadContext.getUnconsumedFileNames();
                log.error("체크리스트 생성 실패 - 미사용 파일 {} 개: {}", unconsumed.size(), unconsumed);
                throw new BusinessException(ErrorCode.CHECK_LIST_FILE_MAPPING_NOT_FOUND);
            }

            eventPublisher.publishEvent(new CheckListCreatedEvent(projectId, nodeId));
          
            return CheckListResponse.from(
                    checkList,
                    checkListService.resolveUserInfo(checkList.getUserId()),
                    itemResponses
            );
        } catch (Exception e) {
            rollbackUploadedFiles(uploadedFileNames);
            throw e;
        }
    }

    /**
     * CheckList 엔티티를 생성하고 저장한다.
     * @param request CheckList 생성 요청
     * @param userId 사용자 식별자
     * @return 저장된 CheckList 엔티티
     */
    private CheckList createCheckList(CheckListCreateRequest request, Long userId, Long nodeId) {
        CheckList checkList = CheckList.of(request.description(), userId, nodeId);
        return checkListService.saveCheckList(checkList);
    }

    /**
     * CheckListItem 목록을 생성하고 히스토리를 기록한다.
     * @param checkListId CheckList 식별자
     * @param itemRequests CheckListItem 생성 요청 목록
     * @param userId 사용자 식별자
     * @return CheckListItemResponse 목록
     */
    private List<CheckListItemResponse> createCheckListItems(Long checkListId,
                                                            List<CheckListItemRequest> itemRequests,
                                                            Long userId,
                                                            CheckListFileUploadContext uploadContext) {

        return itemRequests.stream().map(itemRequest ->
                createCheckListItem(checkListId, itemRequest, userId, uploadContext))
                .collect(Collectors.toList());
    }

    /**
     * 단일 CheckListItem을 생성하고 히스토리를 기록한다.
     * @param checkListId CheckList 식별자
     * @param itemRequest CheckListItem 생성 요청
     * @param userId 사용자 식별자
     * @return CheckListItemResponse
     */
    private CheckListItemResponse createCheckListItem(Long checkListId,
                                                      CheckListItemRequest itemRequest,
                                                      Long userId,
                                                      CheckListFileUploadContext uploadContext) {
        // Item 생성 및 저장
        CheckListItem item = CheckListItem.of(checkListId, itemRequest, userId);
        item = checkListService.saveCheckListItem(item);

        // 히스토리 기록
        checkListService.snapShotAndRecordHistory(item, item.getCheckListItemId(), ActionType.CREATE);

        // Option 목록 생성
        List<CheckListOptionResponse> optionResponses =
                createCheckListOptions(item.getCheckListItemId(), itemRequest.options(), uploadContext);

        return CheckListItemResponse.from(item, optionResponses);
    }

    /**
     * CheckListOption 목록을 생성한다.
     * @param itemId CheckListItem 식별자
     * @param optionRequests CheckListOption 생성 요청 목록
     * @return CheckListOptionResponse 목록
     */
    private List<CheckListOptionResponse> createCheckListOptions(Long itemId,
                                                                 List<CheckListOptionRequest> optionRequests,
                                                                 CheckListFileUploadContext uploadContext) {

        return optionRequests.stream().map(optionRequest ->
                createCheckListOption(itemId, optionRequest, uploadContext))
                .collect(Collectors.toList());
    }

    /**
     * 단일 CheckListOption을 생성한다.
     * @param itemId CheckListItem 식별자
     * @param optionRequest CheckListOption 생성 요청
     * @return CheckListOptionResponse
     */
    private CheckListOptionResponse createCheckListOption(Long itemId,
                                                         CheckListOptionRequest optionRequest,
                                                         CheckListFileUploadContext uploadContext) {
        CheckListOption option = CheckListOption.of(itemId, optionRequest);
        checkListService.saveCheckListOption(option);

        List<CheckListOptionFileResponse> fileResponses =
                createCheckListOptionFiles(option.getCheckListOptionId(), optionRequest.fileUrls(), uploadContext);

        return CheckListOptionResponse.from(option, fileResponses);
    }

    /**
     * CheckListOptionFile 목록을 생성한다.
     * @param optionId CheckListOption 식별자
     * @param fileUrls 파일 URL 목록
     * @return CheckListOptionFileResponse 목록
     */
    private List<CheckListOptionFileResponse> createCheckListOptionFiles(Long optionId,
                                                                        List<String> fileUrls,
                                                                        CheckListFileUploadContext uploadContext) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return new ArrayList<>();
        }

        List<CheckListOptionFileResponse> fileResponses = new ArrayList<>();
        for (int i = 0; i < fileUrls.size(); i++) {
            String identifier = fileUrls.get(i);
            if (identifier == null || identifier.isBlank()) {
                continue;
            }

            CheckListOptionFile optionFile = resolveOptionFile(optionId, identifier, i, uploadContext);
            CheckListOptionFile saved = checkListService.saveCheckListOptionFile(optionFile);
            fileResponses.add(CheckListOptionFileResponse.from(saved));
        }

        return fileResponses;
    }

    private CheckListOptionFile resolveOptionFile(Long optionId,
                                                  String identifier,
                                                  int order,
                                                  CheckListFileUploadContext uploadContext) {
        if (uploadContext != null) {
            Optional<FileUploadResponse> upload = uploadContext.consume(identifier);
            if (upload.isPresent()) {
                return CheckListOptionFile.fromUpload(optionId, upload.get(), order);
            }
        }

        if (isValidRemoteUrl(identifier)) {
            return CheckListOptionFile.of(optionId, identifier, order);
        }

        String availableFiles = uploadContext != null
                ? String.join(", ", uploadContext.getUploadedFileNames())
                : "없음";

        log.error("파일 매핑 실패 - 요청 파일: '{}', 업로드된 파일: [{}]", identifier, availableFiles);

        throw new BusinessException(ErrorCode.CHECK_LIST_FILE_MAPPING_NOT_FOUND);
    }

    private boolean isValidRemoteUrl(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return false;
        }

        // http:// 또는 https://로 시작하는 문자열은 외부 링크로 간주
        return identifier.startsWith("http://") || identifier.startsWith("https://");
    }

    private void rollbackUploadedFiles(List<String> uploadedFileNames) {
        if (uploadedFileNames == null || uploadedFileNames.isEmpty()) {
            return;
        }

        try {
            log.warn("체크리스트 생성 실패로 업로드된 파일 {}개를 삭제합니다.", uploadedFileNames.size());
            fileService.deleteFiles(uploadedFileNames);
        } catch (Exception deleteException) {
            log.error("체크리스트 업로드 파일 삭제 중 오류 발생: {}", uploadedFileNames, deleteException);
        }
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
