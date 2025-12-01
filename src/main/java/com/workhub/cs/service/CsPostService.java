package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostFileUpdateRequest;
import com.workhub.cs.dto.CsPostRequest;
import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.dto.CsPostUpdateRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostFile;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.cs.repository.CsPostFileRepository;
import com.workhub.cs.repository.CsPostRepository;
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
public class CsPostService {

    private final CsPostRepository csPostRepository;
    private final CsPostFileRepository csPostFileRepository;

    /**
     * CS 게시글을 작성합니다.
     * @param projectId 프로젝트 식별자
     * @param userId 작성자 ID
     * @param request 게시글 생성 요청
     * @return CsPostResponse
     */
    public CsPostResponse create(Long projectId, Long userId, CsPostRequest request) {

        CsPost csPost = csPostRepository.save(CsPost.of(projectId, userId, request));

        List<CsPostFile> files = List.of();

        if (request.files() != null && !request.files().isEmpty()) {

            files = request.files().stream()
                    .map(f -> CsPostFile.of(csPost.getCsPostId(), f))
                    .toList();

            csPostFileRepository.saveAll(files);
        }

        return CsPostResponse.from(csPost, files);
    }

    /**
     * 게시글을 업데이트합니다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param userId 요청자 ID
     * @param request 게시글 수정 요청
     * @return CsPostResponse
     */
    public CsPostResponse update(Long projectId, Long csPostId, Long userId, CsPostUpdateRequest request) {

        CsPost csPost = getAndValidatePost(projectId, csPostId);

        // 작성자 검증
        if (!csPost.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_CS_POST_UPDATE);
        }

        updatePost(csPost, request);

        List<CsPostFile> updatedFiles = updateFiles(csPostId, request.files());

        // 삭제되지 않은 파일만 응답
        List<CsPostFile> visibleFiles = updatedFiles.stream().filter(
                file -> file.getDeletedAt() == null).toList();

        return CsPostResponse.from(csPost, visibleFiles);
    }

    /**
     * 게시글을 삭제합니다.
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @return csPostId 삭제된 게시글 식별자
     */
    public Long delete(Long projectId, Long csPostId) {

        CsPost csPost = csPostRepository.findById(csPostId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CS_POST));

        if (csPost.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_CS_POST);
        }

        csPost.validateProject(projectId);
        csPost.markDeleted();

        return csPost.getCsPostId();
    }

    /**
     *
     * @param projectId 프로젝트 식별자
     * @param csPostId 게시글 식별자
     * @param status 게시글 상태 값
     * @return status 변경된 게시글 상태 값
     */
    public CsPostStatus changeStatus(Long projectId, Long csPostId, CsPostStatus status) {
        CsPost csPost = getAndValidatePost(projectId, csPostId);

        csPost.changeStatus(status);

        return csPost.getCsPostStatus();
    }

    /**
     * 파일이 존재하면 업데이트 (삭제, 추가, 유지)
     * @param csPostId
     * @param fileRequests
     * @return
     */
    private List<CsPostFile> updateFiles(Long csPostId, List<CsPostFileUpdateRequest> fileRequests) {

        // 기존 파일 조회
        List<CsPostFile> existingFiles = csPostFileRepository.findByCsPostId(csPostId);

        // 요청 파일이 없으면 기존 파일 그대로 반환
        if (fileRequests == null || fileRequests.isEmpty()) {
            return existingFiles;
        }

        // 기존 파일을 Map으로 바꾸기
        Map<Long, CsPostFile> existingFileMap = existingFiles.stream()
                .collect(Collectors.toMap(
                        CsPostFile::getCsPostFileId,
                        file -> file
                ));

        // 요청으로 받은 파일들 중 filedId 만 모으기
        Set<Long> requestedIds = fileRequests.stream()
                .map(CsPostFileUpdateRequest::fileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 기존 파일 중 요청에 없는 파일은 삭제
        for (CsPostFile existingFile : existingFiles) {
            if (!requestedIds.contains(existingFile.getCsPostFileId())) {
                existingFile.markDeleted();
            }
        }

        List<CsPostFile> resultFiles = new ArrayList<>();

        for (CsPostFileUpdateRequest req : fileRequests) {
            if (req.fileId() == null) {
                if (req.deleted()) {
                    throw new BusinessException(ErrorCode.INVALID_FILE_UPDATE);
                }
                CsPostFile newFile = CsPostFile.of(csPostId, req);
                resultFiles.add(csPostFileRepository.save(newFile));
                continue;
            }

            CsPostFile target = existingFileMap.get(req.fileId());
            if (target == null) {
                throw new BusinessException(ErrorCode.NOT_EXISTS_CS_POST_FILE);
            }
            if (req.deleted()) {
                target.markDeleted();
                continue;
            }

            // 유지 or 순서 업데이트
            target.updateOrder(req.fileOrder());
            resultFiles.add(target);
        }

        return resultFiles;
    }

    /**
     * 게시물 제목 + 내용 수정
     * @param csPost
     * @param request
     */
    private void updatePost(CsPost csPost, CsPostUpdateRequest request) {
        if (request.title() != null && !request.title().isBlank()) {
            csPost.updateTitle(request.title());
        }

        if (request.content() != null && !request.content().isBlank()) {
            csPost.updateContent(request.content());
        }

        csPostRepository.save(csPost);
    }

    /**
     * 게시글 조회 + 프로젝트 소속 검증
     * @param projectId
     * @param csPostId
     * @return
     */
    private CsPost getAndValidatePost(Long projectId, Long csPostId) {

        CsPost post = csPostRepository.findById(csPostId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_EXISTS_CS_POST));

        // 프로젝트 소속 검증
        if (!projectId.equals(post.getProjectId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_CS_POST);
        }

        return post;
    }

}
