package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.record.request.PostFileUpdateRequest;
import com.workhub.post.record.request.PostUpdateRequest;
import com.workhub.post.record.response.PostResponse;
import com.workhub.project.service.ProjectService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdatePostService {

    private final PostService postService;
    private final ProjectService projectService;

    /**
     * 프로젝트와 노드 일치 여부, 작성자 권한을 검증한 뒤 게시글을 수정한다.
     */
    public PostResponse update(Long projectId, Long nodeId, Long postId, Long userId, PostUpdateRequest request) {
        projectService.validateProject(projectId);
        Post target = postService.findById(postId);
        postService.validateNode(target, nodeId);
        if (!target.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_POST_UPDATE);
        }

        target.update(request);

        List<PostFile> updatedFiles = updateFiles(postId, request.files());
        List<PostFile> visibleFiles = updatedFiles.stream()
                .filter(file -> file.getDeletedAt() == null)
                .toList();

        return PostResponse.from(target, visibleFiles);
    }

    /**
     * 첨부 파일 수정 요청을 반영해 추가/삭제/순서 변경을 처리한다.
     *
     * @param postId 게시글 ID
     * @param fileRequests 파일 수정 요청 목록
     * @return 최신 파일 목록
     */
    private List<PostFile> updateFiles(Long postId, List<PostFileUpdateRequest> fileRequests) {
        List<PostFile> existingFiles = postService.findFilesByPostId(postId);

        if (fileRequests == null || fileRequests.isEmpty()) {
            return existingFiles;
        }

        Map<Long, PostFile> existingFileMap = mapExistingFiles(existingFiles);
        markRemovedFiles(existingFiles, extractRequestedIds(fileRequests));

        List<PostFile> resultFiles = new ArrayList<>();
        for (PostFileUpdateRequest req : fileRequests) {
            PostFile processed = handleFileRequest(postId, req, existingFileMap);
            if (processed != null) {
                resultFiles.add(processed);
            }
        }
        return resultFiles;
    }

    /** 기존 파일을 빠르게 찾기 위해 ID 기반으로 맵을 구성한다. */
    private Map<Long, PostFile> mapExistingFiles(List<PostFile> existingFiles) {
        return existingFiles.stream()
                .collect(Collectors.toMap(PostFile::getPostFileId, file -> file));
    }

    /** 요청에서 전달된 파일 ID만 추출한다. */
    private Set<Long> extractRequestedIds(List<PostFileUpdateRequest> fileRequests) {
        return fileRequests.stream()
                .map(PostFileUpdateRequest::fileId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** 요청 목록에 포함되지 않은 기존 파일을 삭제 처리한다. */
    private void markRemovedFiles(List<PostFile> existingFiles, Set<Long> requestedIds) {
        for (PostFile existingFile : existingFiles) {
            if (!requestedIds.contains(existingFile.getPostFileId())) {
                existingFile.markDeleted();
            }
        }
    }

    /**
     * 단일 파일 수정 요청을 처리한다.
     *
     * @param postId 게시글 ID
     * @param req 파일 요청
     * @param existingFileMap 기존 파일 맵
     * @return 유지/추가된 파일 (삭제 시 null)
     */
    private PostFile handleFileRequest(Long postId,
                                       PostFileUpdateRequest req,
                                       Map<Long, PostFile> existingFileMap) {
        if (req.fileId() == null) {
            return addNewFile(postId, req);
        }

        PostFile target = existingFileMap.get(req.fileId());
        if (target == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_POST_FILE);
        }

        if (req.deleted()) {
            target.markDeleted();
            return null;
        }

        target.updateOrder(req.fileOrder());
        return target;
    }

    /**
     * 신규 파일을 생성하고 저장한다.
     *
     * @param postId 게시글 ID
     * @param req 파일 요청
     * @return 저장된 파일
     */
    private PostFile addNewFile(Long postId, PostFileUpdateRequest req) {
        if (req.deleted()) {
            throw new BusinessException(ErrorCode.INVALID_POST_FILE_UPDATE);
        }
        PostFile newFile = PostFile.of(postId, req);
        return postService.savePostFile(newFile);
    }
}
