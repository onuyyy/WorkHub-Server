package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentFileRequest;
import com.workhub.checklist.dto.comment.CheckListCommentFileResponse;
import com.workhub.checklist.dto.comment.CheckListCommentRequest;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.event.CheckListCommentCreatedEvent;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
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

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CreateCheckListCommentService {

    private final CheckListCommentService checkListCommentService;
    private final ApplicationEventPublisher eventPublisher;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListService checkListService;
    private final FileService fileService;

    /**
     * 프로젝트 해당하는 개발사/고객사와 관리자만이 댓글을 작성할 수 있다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param checkListId 체크리스트 식별자
     * @param checkListItemId 아이템 식별자
     * @param request 요청 폼
     * @param files 업로드 파일 목록
     * @return CheckListCommentResponse
     */
    public CheckListCommentResponse create(
            Long projectId, Long nodeId, Long checkListId, Long checkListItemId,
            CheckListCommentRequest request, List<MultipartFile> files) {

        // 1. 검증
        ValidatedContext context = validateAndPrepare(projectId, nodeId, checkListId, checkListItemId, request);

        // 2. 파일 업로드
        Map<String, FileUploadResponse> uploadMap = uploadFilesToS3(files);

        // 3. 댓글 저장
        CheckListItemComment comment = saveComment(checkListItemId, context.parentComment(), request.content());

        // 4. 파일 저장 및 응답 생성
        List<CheckListCommentFileResponse> fileResponses = processAndSaveFiles(
                comment.getClCommentId(), request.files(), uploadMap);

        // 5. 이벤트 발행
        publishCommentCreatedEvent(projectId, nodeId, checkListId, checkListItemId,
                comment, context.checkList(), context.parentComment());

        return CheckListCommentResponse.from(comment, fileResponses);
    }

    // 프로젝트/노드/항목/부모 댓글을 검증하고 결과를 묶는다.
    private ValidatedContext validateAndPrepare(Long projectId, Long nodeId, Long checkListId,
                                                 Long checkListItemId, CheckListCommentRequest request) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);
        validateContent(request.content());

        CheckList checkList = checkListService.findById(checkListId);
        validateCheckListBelongsToNode(nodeId, checkList);

        CheckListItem checkListItem = checkListService.findCheckListItem(checkListItemId);
        validateItemBelongsToCheckList(checkListId, checkListItem);

        CheckListItemComment parentComment = resolveParent(checkListItemId, request.patentClCommentId());

        return new ValidatedContext(checkList, parentComment);
    }

    // 업로드한 파일을 S3에 저장하고 원본 파일명 기준으로 매핑한다.
    private Map<String, FileUploadResponse> uploadFilesToS3(List<MultipartFile> files) {
        List<FileUploadResponse> uploadResponses = fileService.uploadFiles(files);
        Map<String, FileUploadResponse> uploadMap = new HashMap<>();
        if (uploadResponses != null) {
            for (FileUploadResponse response : uploadResponses) {
                uploadMap.put(response.originalFileName(), response);
            }
        }
        return uploadMap;
    }

    // 댓글 엔티티를 저장하고 이력을 남긴다.
    private CheckListItemComment saveComment(Long checkListItemId, CheckListItemComment parentComment, String content) {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        CheckListItemComment comment = CheckListItemComment.of(
                checkListItemId,
                userId,
                parentComment != null ? parentComment.getClCommentId() : null,
                content
        );
        comment = checkListCommentService.save(comment);
        checkListService.snapShotAndRecordHistory(comment, comment.getClCommentId(), ActionType.CREATE);
        return comment;
    }

    // 업로드 정보와 요청 정보를 매칭해 파일 엔티티를 저장하고 실패 시 업로드를 롤백한다.
    private List<CheckListCommentFileResponse> processAndSaveFiles(Long commentId,
                                                                    List<CheckListCommentFileRequest> fileRequests,
                                                                    Map<String, FileUploadResponse> uploadMap) {
        try {
            List<CheckListCommentFileResponse> fileResponses = createCommentFiles(commentId, fileRequests, uploadMap);

            if (uploadMap.size() != (fileRequests != null ? fileRequests.size() : 0)) {
                log.warn("댓글 생성 시 업로드된 파일 수와 요청 파일 수가 일치하지 않습니다.");
            }

            return fileResponses;
        } catch (Exception e) {
            rollbackUploadedFiles(new ArrayList<>(uploadMap.values()));
            throw e;
        }
    }

    // 댓글 생성 시 알림 및 후처리를 위한 이벤트를 발행한다.
    private void publishCommentCreatedEvent(Long projectId, Long nodeId, Long checkListId,
                                            Long checkListItemId, CheckListItemComment comment,
                                            CheckList checkList, CheckListItemComment parentComment) {
        Long userId = SecurityUtil.getCurrentUserIdOrThrow();
        eventPublisher.publishEvent(new CheckListCommentCreatedEvent(
                projectId,
                nodeId,
                checkListId,
                checkListItemId,
                comment.getClCommentId(),
                comment.getClContent(),
                checkList.getUserId(),
                parentComment != null ? parentComment.getUserId() : null,
                userId
        ));
    }

    private record ValidatedContext(CheckList checkList, CheckListItemComment parentComment) {
    }

    // 댓글 본문이 공백인지 확인한다.
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_ITEM_COMMENT_CONTENT);
        }
    }

    // 체크리스트가 요청한 노드에 속하는지 검증한다.
    private void validateCheckListBelongsToNode(Long nodeId, CheckList checkList) {
        if (!nodeId.equals(checkList.getProjectNodeId())) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST);
        }
    }

    // 항목이 지정한 체크리스트에 속하는지 검증한다.
    private void validateItemBelongsToCheckList(Long checkListId, CheckListItem checkListItem) {
        if (!checkListId.equals(checkListItem.getCheckListId())) {
            throw new BusinessException(ErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST);
        }
    }

    // 부모 댓글이 존재하면 같은 항목인지 확인하고 반환한다.
    private CheckListItemComment resolveParent(Long checkListItemId, Long parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        CheckListItemComment parentComment = checkListCommentService.findById(parentCommentId);
        if (!checkListItemId.equals(parentComment.getCheckListItemId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CHECK_LIST_ITEM_COMMENT);
        }
        return parentComment;
    }

    /**
     * 댓글 첨부파일 목록을 생성한다.
     * @param commentId 댓글 식별자
     * @param fileRequests 파일 요청 목록
     * @param uploadMap 업로드된 파일 맵
     * @return CheckListCommentFileResponse 목록
     */
    private List<CheckListCommentFileResponse> createCommentFiles(
            Long commentId,
            List<CheckListCommentFileRequest> fileRequests,
            Map<String, FileUploadResponse> uploadMap) {
        if (fileRequests == null || fileRequests.isEmpty()) {
            return List.of();
        }

        List<CheckListCommentFileResponse> responses = new ArrayList<>();
        for (CheckListCommentFileRequest fileRequest : fileRequests) {
            FileUploadResponse uploadResponse = uploadMap.get(fileRequest.fileName());

            CheckListItemCommentFile commentFile;
            if (uploadResponse != null) {
                // S3에 업로드된 파일
                commentFile = CheckListItemCommentFile.fromUpload(commentId, uploadResponse, fileRequest.fileOrder());
            } else {
                // 업로드 파일을 찾을 수 없는 경우 예외 발생
                log.error("파일 매핑 실패 - 요청 파일: '{}', 업로드된 파일: [{}]",
                        fileRequest.fileName(), String.join(", ", uploadMap.keySet()));
                throw new BusinessException(ErrorCode.CHECK_LIST_FILE_MAPPING_NOT_FOUND);
            }

            checkListCommentService.saveCommentFile(commentFile);
            responses.add(CheckListCommentFileResponse.from(commentFile));
        }

        return responses;
    }

    // 댓글 생성이 실패했을 때 이미 업로드한 파일을 정리한다.
    private void rollbackUploadedFiles(List<FileUploadResponse> uploadResponses) {
        if (uploadResponses == null || uploadResponses.isEmpty()) {
            return;
        }

        try {
            List<String> fileNames = uploadResponses.stream()
                    .map(FileUploadResponse::fileName)
                    .toList();
            log.warn("댓글 생성 실패로 업로드된 파일 {}개를 삭제합니다.", fileNames.size());
            fileService.deleteFiles(fileNames);
        } catch (Exception deleteException) {
            log.error("댓글 업로드 파일 삭제 중 오류 발생", deleteException);
        }
    }
}
