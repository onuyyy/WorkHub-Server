package com.workhub.cs.service.csPost;

import com.workhub.cs.dto.csPost.CsPostFileUpdateRequest;
import com.workhub.cs.dto.csPost.CsPostResponse;
import com.workhub.cs.dto.csPost.CsPostUpdateRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.cs.service.CsPostNotificationService;
import com.workhub.cs.service.csQna.CsQnaService;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateCsPostService {

    private final CsPostService csPostService;
    private final CsPostAccessValidator csPostAccessValidator;
    private final CsPostNotificationService csPostNotificationService;
    private final CsQnaService csQnaService;

    /**
     * CS POST를 수정한다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param userId 작성자 식별자
     * @param request 수정 요청 DTO
     * @return CsPostResponse 수정 후 결과
     */
    public CsPostResponse update(Long projectId, Long csPostId, Long userId, CsPostUpdateRequest request) {

        CsPost csPost = csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId);

        if (!csPost.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_CS_POST_UPDATE);
        }

        updatePost(csPost, request);

        List<CsPostFile> updatedFiles = updateFiles(csPostId, request.files());

        List<CsPostFile> visibleFiles = updatedFiles.stream().filter(
                file -> file.getDeletedAt() == null).toList();

        Set<Long> commenters = csQnaService.findAllByCsPostId(csPostId).stream()
                .map(CsQna::getUserId)
                .collect(Collectors.toSet());
        csPostNotificationService.notifyCsPostUpdated(projectId, csPostId, csPost.getTitle(), commenters);

        return CsPostResponse.from(csPost, visibleFiles);
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
     * @return List<CsPostFile> 수정 후 파일 목록
     */
    private List<CsPostFile> updateFiles(Long csPostId, List<CsPostFileUpdateRequest> fileRequests) {

        List<CsPostFile> existingFiles = csPostService.findFilesByCsPostId(csPostId);

        if (fileRequests == null || fileRequests.isEmpty()) {
            return existingFiles;
        }

        Map<Long, CsPostFile> existingFileMap = mapExistingFiles(existingFiles);
        markRemovedFiles(existingFiles, extractRequestedIds(fileRequests));

        List<CsPostFile> resultFiles = new ArrayList<>();

        for (CsPostFileUpdateRequest req : fileRequests) {
            CsPostFile processed = handleFileRequest(csPostId, req, existingFileMap);
            if (processed != null) {
                resultFiles.add(processed);
            }
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
     */
    private void markRemovedFiles(List<CsPostFile> existingFiles, Set<Long> requestedIds) {
        for (CsPostFile existingFile : existingFiles) {
            if (!requestedIds.contains(existingFile.getCsPostFileId())) {
                existingFile.markDeleted();
            }
        }
    }

    /**
     * 단일 파일 수정 요청을 처리한다.
     * @param csPostId 게시글 식별자
     * @param req 파일 수정 요청
     * @param existingFileMap 기존 파일 맵
     * @return 유지/추가된 파일 (삭제 시 null)
     */
    private CsPostFile handleFileRequest(Long csPostId,
                                         CsPostFileUpdateRequest req,
                                         Map<Long, CsPostFile> existingFileMap) {

        if (req.fileId() == null) {
            return addNewFile(csPostId, req);
        }

        CsPostFile target = existingFileMap.get(req.fileId());
        if (target == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_CS_POST_FILE);
        }

        if (req.deleted()) {
            target.markDeleted();
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


}
