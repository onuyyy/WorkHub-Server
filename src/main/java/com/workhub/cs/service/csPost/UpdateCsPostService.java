package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostFileUpdateRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.dto.csPost.CsPostUpdateRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.port.AuthorLookupPort;
import com.workhub.cs.port.dto.AuthorProfile;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.cs.service.csQna.CsQnaService;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCsPostService {

    private final CsPostService csPostService;
    private final CsPostAccessValidator csPostAccessValidator;
    private final CsPostNotificationService csPostNotificationService;
    private final CsQnaService csQnaService;
    private final FileService fileService;
    private final AuthorLookupPort authorLookupPort;

    /**
     * CS POST를 수정한다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param userId 작성자 식별자
     * @param request 수정 요청 DTO
     * @param newFiles 새로 추가할 파일 리스트
     * @return CsPostResponse 수정 후 결과
     */
    public CsPostResponse update(Long projectId, Long csPostId, Long userId, CsPostUpdateRequest request, List<MultipartFile> newFiles) {

        CsPost csPost = csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId);

        if (!csPost.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_CS_POST_UPDATE);
        }

        // 업로드된 파일명을 추적하여 예외 발생 시 삭제
        List<String> uploadedFileNames = new ArrayList<>();
        // 삭제될 파일명을 추적하여 성공 시 S3에서 삭제
        List<String> filesToDelete = new ArrayList<>();

        try {
            updatePost(csPost, request);

            List<CsPostFile> updatedFiles = updateFiles(csPostId, request.files(), newFiles, uploadedFileNames, filesToDelete);

            List<CsPostFile> visibleFiles = updatedFiles.stream().filter(
                    file -> file.getDeletedAt() == null).toList();

            // 삭제된 파일들을 S3에서도 삭제
            deleteFilesFromS3(filesToDelete);

            Set<Long> commenters = csQnaService.findAllByCsPostId(csPostId).stream()
                    .map(CsQna::getUserId)
                    .collect(Collectors.toSet());
            csPostNotificationService.notifyUpdated(projectId, csPostId, csPost.getTitle(), commenters);

        String userName = authorLookupPort.findByUserId(csPost.getUserId())
                .map(AuthorProfile::userName)
                .orElse(null);

        return CsPostResponse.from(csPost, visibleFiles, userName);

        } catch (Exception e) {
            // 예외 발생 시 업로드된 S3 파일 삭제 (Best Effort)
            rollbackUploadedFiles(uploadedFileNames);
            // 원래 예외를 다시 throw하여 트랜잭션 롤백 및 사용자에게 에러 응답
            throw e;
        }
    }

    /**
     * CS POST 진행 상황 상태값을 변경한다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param status 변경할 상태
     * @return CsPostStatus 변경 결과
     */
    public CsPostStatus changeStatus(Long projectId, Long csPostId, CsPostStatus status) {
        CsPost csPost = csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId);

        csPostService.snapShotAndRecordHistory(csPost, csPost.getCsPostId(), ActionType.UPDATE);

        csPost.changeStatus(status);

        return csPost.getCsPostStatus();
    }

    /**
     * CS POST의 파일을 수정한다.
     * @param csPostId 게시글 식별자
     * @param fileRequests 파일 수정 요청 목록
     * @param newFiles 새로 추가할 파일 리스트
     * @param uploadedFileNames 업로드된 파일명 추적 리스트 (롤백용)
     * @param filesToDelete 삭제할 파일명 추적 리스트 (S3 삭제용)
     * @return List<CsPostFile> 수정 후 파일 목록
     */
    private List<CsPostFile> updateFiles(Long csPostId,
                                         List<CsPostFileUpdateRequest> fileRequests,
                                         List<MultipartFile> newFiles,
                                         List<String> uploadedFileNames,
                                         List<String> filesToDelete) {

        List<CsPostFile> existingFiles = csPostService.findFilesByCsPostId(csPostId);

        if (fileRequests == null || fileRequests.isEmpty()) {
            // 새 파일만 업로드
            if (newFiles != null && !newFiles.isEmpty()) {
                return uploadAndSaveNewFiles(csPostId, newFiles, uploadedFileNames);
            }
            return existingFiles;
        }

        Map<Long, CsPostFile> existingFileMap = mapExistingFiles(existingFiles);
        markRemovedFiles(existingFiles, extractRequestedIds(fileRequests), filesToDelete);

        List<CsPostFile> resultFiles = new ArrayList<>();

        // 기존 파일 처리
        for (CsPostFileUpdateRequest req : fileRequests) {
            CsPostFile processed = handleFileRequest(csPostId, req, existingFileMap, filesToDelete);
            if (processed != null) {
                resultFiles.add(processed);
            }
        }

        // 새 파일 업로드 및 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            List<CsPostFile> newlyUploadedFiles = uploadAndSaveNewFiles(csPostId, newFiles, uploadedFileNames);
            resultFiles.addAll(newlyUploadedFiles);
        }

        return resultFiles;
    }

    /**
     * 기존 첨부 파일을 ID 키 기반 Map으로 만든다.
     * @param existingFiles 기존 파일 목록
     * @return Map<파일ID, 파일 엔티티>
     */
    private Map<Long, CsPostFile> mapExistingFiles(List<CsPostFile> existingFiles) {
        return existingFiles.stream()
                .collect(Collectors.toMap(CsPostFile::getCsPostFileId, file -> file));
    }

    /**
     * 요청에 포함된 파일 ID만 추출한다.
     * @param fileRequests 파일 수정 요청
     * @return 요청된 파일 ID 집합
     */
    private Set<Long> extractRequestedIds(List<CsPostFileUpdateRequest> fileRequests) {
        return fileRequests.stream()
                .map(CsPostFileUpdateRequest::fileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * 요청에서 빠진 기존 파일을 삭제 처리한다.
     * @param existingFiles 기존 파일 목록
     * @param requestedIds 유지/수정할 파일 ID
     * @param filesToDelete 삭제할 파일명 추적 리스트
     */
    private void markRemovedFiles(List<CsPostFile> existingFiles, Set<Long> requestedIds, List<String> filesToDelete) {
        for (CsPostFile existingFile : existingFiles) {
            if (!requestedIds.contains(existingFile.getCsPostFileId())) {
                existingFile.markDeleted();
                // S3에서도 삭제하기 위해 추적
                if (existingFile.getFileUrl() != null) {
                    filesToDelete.add(existingFile.getFileUrl());
                }
            }
        }
    }

    /**
     * 단일 파일 수정 요청을 처리한다.
     * @param csPostId 게시글 식별자
     * @param req 파일 수정 요청
     * @param existingFileMap 기존 파일 맵
     * @param filesToDelete 삭제할 파일명 추적 리스트
     * @return 유지/추가된 파일 (삭제 시 null)
     */
    private CsPostFile handleFileRequest(Long csPostId,
                                         CsPostFileUpdateRequest req,
                                         Map<Long, CsPostFile> existingFileMap,
                                         List<String> filesToDelete) {

        if (req.fileId() == null) {
            return addNewFile(csPostId, req);
        }

        CsPostFile target = existingFileMap.get(req.fileId());
        if (target == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CS_POST_FILE);
        }

        if (req.deleted()) {
            target.markDeleted();
            // S3에서도 삭제하기 위해 추적
            if (target.getFileUrl() != null) {
                filesToDelete.add(target.getFileUrl());
            }
            return null;
        }

        target.updateOrder(req.fileOrder());
        return target;
    }

    /**
     * 새 파일을 추가한다.
     * @param csPostId 게시글 식별자
     * @param req 파일 생성 요청
     * @return 저장된 파일 엔티티
     */
    private CsPostFile addNewFile(Long csPostId, CsPostFileUpdateRequest req) {
        if (req.deleted()) {
            throw new BusinessException(ErrorCode.INVALID_FILE_UPDATE);
        }
        CsPostFile newFile = CsPostFile.of(csPostId, req);

        return csPostService.saveFile(newFile);
    }

    /**
     * CS POST 게시글을 수정한다.
     * @param csPost 기존 게시글
     * @param request 수정 요청 DTO
     */
    private void updatePost(CsPost csPost, CsPostUpdateRequest request) {
        if (request.title() != null && !request.title().isBlank()) {
            csPostService.snapShotAndRecordHistory(csPost, csPost.getCsPostId(), ActionType.UPDATE);
            csPost.updateTitle(request.title());
        }

        if (request.content() != null && !request.content().isBlank()) {
            csPostService.snapShotAndRecordHistory(csPost, csPost.getCsPostId(), ActionType.UPDATE);
            csPost.updateContent(request.content());
        }

    }

    /**
     * 업로드된 S3 파일을 삭제 (Best Effort).
     * 삭제 실패 시에도 예외를 throw하지 않고 로그만 기록합니다.
     * @param uploadedFileNames 삭제할 파일명 리스트
     */
    private void rollbackUploadedFiles(List<String> uploadedFileNames) {
        if (uploadedFileNames == null || uploadedFileNames.isEmpty()) {
            return;
        }

        try {
            log.warn("CS 게시글 수정 실패로 인해 업로드된 파일 {} 개를 삭제합니다.", uploadedFileNames.size());
            fileService.deleteFiles(uploadedFileNames);
            log.info("업로드된 파일 {} 개 삭제 완료", uploadedFileNames.size());
        } catch (Exception deleteException) {
            log.error("업로드된 파일 삭제 중 오류 발생 (파일은 S3에 남아있을 수 있음): {}", uploadedFileNames, deleteException);
        }
    }

    /**
     * S3에서 파일을 삭제.
     * @param filesToDelete 삭제할 파일명 리스트
     */
    private void deleteFilesFromS3(List<String> filesToDelete) {
        if (filesToDelete == null || filesToDelete.isEmpty()) {
            return;
        }

        try {
            log.info("CS 게시글에서 삭제된 파일 {} 개를 S3에서 삭제합니다.", filesToDelete.size());
            fileService.deleteFiles(filesToDelete);
            log.info("S3에서 파일 {} 개 삭제 완료", filesToDelete.size());
        } catch (Exception deleteException) {
            log.error("S3 파일 삭제 중 오류 발생: {}", filesToDelete, deleteException);
            // S3 삭제 실패는 전체 트랜잭션을 롤백하지 않음 (Best Effort)
        }
    }

    /**
     * 새 파일들을 S3에 업로드하고 CsPostFile 엔티티를 생성하여 저장.
     * @param csPostId CS 게시글 ID
     * @param newFiles 업로드할 파일 리스트
     * @param uploadedFileNames 업로드된 파일명 추적 리스트
     * @return 저장된 CsPostFile 목록
     */
    private List<CsPostFile> uploadAndSaveNewFiles(Long csPostId, List<MultipartFile> newFiles, List<String> uploadedFileNames) {
        if (newFiles == null || newFiles.isEmpty()) {
            return List.of();
        }

        // 기존 파일의 최대 순서를 찾아서 그 다음부터 시작
        List<CsPostFile> existingFiles = csPostService.findFilesByCsPostId(csPostId);
        int maxOrder = existingFiles.stream()
                .filter(file -> file.getDeletedAt() == null)
                .map(CsPostFile::getFileOrder)
                .max(Integer::compareTo)
                .orElse(0);

        // S3에 파일 업로드
        List<FileUploadResponse> uploadFiles = fileService.uploadFiles(newFiles);

        // CsPostFile 엔티티 생성 및 저장
        List<CsPostFile> csPostFiles = new ArrayList<>();
        for (int i = 0; i < uploadFiles.size(); i++) {
            FileUploadResponse uploadFile = uploadFiles.get(i);
            CsPostFile csPostFile = CsPostFile.of(csPostId, uploadFile, maxOrder + i + 1);
            csPostFiles.add(csPostFile);
            // 업로드된 파일명 추적 (롤백용)
            uploadedFileNames.add(uploadFile.fileName());
        }

        return csPostService.saveAllFiles(csPostFiles);
    }

}
