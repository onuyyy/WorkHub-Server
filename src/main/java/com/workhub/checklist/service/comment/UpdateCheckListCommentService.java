package com.workhub.checklist.service.comment;

import com.workhub.checklist.dto.comment.CheckListCommentFileRequest;
import com.workhub.checklist.dto.comment.CheckListCommentFileResponse;
import com.workhub.checklist.dto.comment.CheckListCommentResponse;
import com.workhub.checklist.dto.comment.CheckListCommentUpdateRequest;
import com.workhub.checklist.entity.checkList.CheckList;
import com.workhub.checklist.entity.checkList.CheckListItem;
import com.workhub.checklist.entity.comment.CheckListItemComment;
import com.workhub.checklist.entity.comment.CheckListItemCommentFile;
import com.workhub.checklist.service.CheckListAccessValidator;
import com.workhub.checklist.service.checkList.CheckListService;
import com.workhub.file.dto.FileUploadResponse;
import com.workhub.file.service.FileService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCheckListCommentService {

    private final CheckListCommentService checkListCommentService;
    private final CheckListAccessValidator checkListAccessValidator;
    private final CheckListService checkListService;
    private final FileService fileService;

    /**
     * 작성자 또는 관리자만이 댓글을 수정할 수 있다.
     * @param projectId 프로젝트 식별자
     * @param nodeId 노드 식별자
     * @param checkListId 체크리스트 식별자
     * @param checkListItemId 아이템 식별자
     * @param commentId 댓글 식별자
     * @param request 요청 폼
     * @param newFiles 신규 업로드 파일
     * @return CheckListCommentResponse
     */
    public CheckListCommentResponse update(Long projectId,
                                           Long nodeId,
                                           Long checkListId,
                                           Long checkListItemId,
                                           Long commentId,
                                           CheckListCommentUpdateRequest request,
                                           List<MultipartFile> newFiles) {

        // 1. 검증 및 댓글 조회
        CheckListItemComment comment = validateAndFindComment(
                projectId, nodeId, checkListId, checkListItemId, commentId, request);

        // 2. 댓글 내용 수정
        updateCommentContent(comment, request.content());

        // 3. 파일 업로드
        Map<String, FileUploadResponse> uploadMap = uploadFilesToS3(newFiles);
        List<String> uploadedFileNames = new ArrayList<>(uploadMap.keySet());

        // 4. 파일 교체 (기존 파일 삭제 + 신규 파일 저장)
        List<CheckListCommentFileResponse> fileResponses = replaceCommentFiles(
                commentId, request.files(), uploadMap, uploadedFileNames);

        return CheckListCommentResponse.from(comment, fileResponses);
    }

    // 수정 가능 여부를 검증하고 대상 댓글을 조회한다.
    private CheckListItemComment validateAndFindComment(Long projectId, Long nodeId, Long checkListId,
                                                         Long checkListItemId, Long commentId,
                                                         CheckListCommentUpdateRequest request) {
        checkListAccessValidator.validateProjectAndNode(projectId, nodeId);
        checkListAccessValidator.checkProjectMemberOrAdmin(projectId);
        validateContent(request.content());

        CheckList checkList = checkListService.findById(checkListId);
        validateCheckListBelongsToNode(nodeId, checkList);

        CheckListItem checkListItem = checkListService.findCheckListItem(checkListItemId);
        validateItemBelongsToCheckList(checkListId, checkListItem);

        CheckListItemComment comment = checkListCommentService.findById(commentId);
        validateCommentBelongsToItem(checkListItemId, comment);
        checkListAccessValidator.validateAdminOrCommentOwner(comment.getUserId());

        return comment;
    }

    // 댓글 내용을 갱신하고 히스토리를 기록한다.
    private void updateCommentContent(CheckListItemComment comment, String content) {
        comment.updateContent(content);
        checkListService.snapShotAndRecordHistory(comment, comment.getClCommentId(), ActionType.UPDATE);
    }

    // 업로드된 신규 파일을 S3에 저장하고 파일명을 매핑한다.
    private Map<String, FileUploadResponse> uploadFilesToS3(List<MultipartFile> newFiles) {
        List<FileUploadResponse> uploadResponses = fileService.uploadFiles(newFiles);
        Map<String, FileUploadResponse> uploadMap = new HashMap<>();
        if (uploadResponses != null) {
            for (FileUploadResponse response : uploadResponses) {
                uploadMap.put(response.originalFileName(), response);
            }
        }
        return uploadMap;
    }

    // 기존 파일을 삭제하고 새 파일을 저장하며 실패 시 업로드를 롤백한다.
    private List<CheckListCommentFileResponse> replaceCommentFiles(Long commentId,
                                                                    List<CheckListCommentFileRequest> fileRequests,
                                                                    Map<String, FileUploadResponse> uploadMap,
                                                                    List<String> uploadedFileNames) {
        List<String> filesToDelete = new ArrayList<>();

        try {
            // 기존 파일 삭제
            deleteExistingFiles(commentId, filesToDelete);

            // 신규 파일 저장
            List<CheckListCommentFileResponse> fileResponses = createCommentFiles(commentId, fileRequests, uploadMap);

            // 미사용 파일 검증
            if (uploadMap.size() != (fileRequests != null ? fileRequests.size() : 0)) {
                log.warn("댓글 수정 시 업로드된 파일 수와 요청 파일 수가 일치하지 않습니다.");
            }

            // S3에서 기존 파일 삭제
            deleteFilesFromS3(filesToDelete);

            return fileResponses;
        } catch (Exception e) {
            rollbackUploadedFiles(uploadedFileNames);
            throw e;
        }
    }

    // DB에 저장된 기존 댓글 파일을 조회해 삭제 대상 목록을 만든다.
    private void deleteExistingFiles(Long commentId, List<String> filesToDelete) {
        List<CheckListItemCommentFile> existingFiles =
                checkListCommentService.findCommentFilesByCommentId(commentId);

        if (!existingFiles.isEmpty()) {
            existingFiles.forEach(file -> {
                if (file.isManagedFile()) {
                    filesToDelete.add(file.getFileUrl());
                }
            });
            checkListCommentService.deleteCommentFiles(existingFiles);
        }
    }

    // 수정 요청 본문이 공백인지 확인한다.
    private void validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_CHECK_LIST_ITEM_COMMENT_CONTENT);
        }
    }

    // 체크리스트가 전달된 노드에 속하는지 검증한다.
    private void validateCheckListBelongsToNode(Long nodeId, CheckList checkList) {
        if (!nodeId.equals(checkList.getProjectNodeId())) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CHECK_LIST);
        }
    }

    // 항목이 특정 체크리스트에 속하는지 검증한다.
    private void validateItemBelongsToCheckList(Long checkListId, CheckListItem checkListItem) {
        if (!checkListId.equals(checkListItem.getCheckListId())) {
            throw new BusinessException(ErrorCode.CHECK_LIST_ITEM_NOT_BELONG_TO_CHECK_LIST);
        }
    }

    // 댓글이 요청한 항목에 속하는지 확인한다.
    private void validateCommentBelongsToItem(Long checkListItemId, CheckListItemComment comment) {
        if (!checkListItemId.equals(comment.getCheckListItemId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_CHECK_LIST_ITEM_COMMENT);
        }
    }

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

    // 실패 시 새로 업로드한 파일을 삭제한다.
    private void rollbackUploadedFiles(List<String> uploadedFileNames) {
        if (uploadedFileNames == null || uploadedFileNames.isEmpty()) {
            return;
        }

        try {
            log.warn("댓글 수정 실패로 업로드된 파일 {}개를 삭제합니다.", uploadedFileNames.size());
            fileService.deleteFiles(uploadedFileNames);
        } catch (Exception deleteException) {
            log.error("댓글 수정 실패 - 업로드 파일 삭제 중 오류 발생: {}", uploadedFileNames, deleteException);
        }
    }

    // 기존에 관리 중이던 파일을 S3에서 삭제한다.
    private void deleteFilesFromS3(List<String> filesToDelete) {
        if (filesToDelete == null || filesToDelete.isEmpty()) {
            return;
        }

        try {
            fileService.deleteFiles(filesToDelete);
        } catch (Exception deleteException) {
            log.error("댓글 파일 삭제 중 오류 발생: {}", filesToDelete, deleteException);
        }
    }

}
