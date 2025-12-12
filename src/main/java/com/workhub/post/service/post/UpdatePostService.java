package com.workhub.post.service.post;

import com.workhub.global.entity.ActionType;
import com.workhub.global.entity.HistoryType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import com.workhub.post.dto.post.PostHistorySnapshot;
import com.workhub.post.dto.post.request.PostFileUpdateRequest;
import com.workhub.post.dto.post.request.PostLinkUpdateRequest;
import com.workhub.post.dto.post.request.PostUpdateRequest;
import com.workhub.post.dto.post.response.PostResponse;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.entity.PostLink;
import com.workhub.post.service.PostValidator;
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
    private final PostValidator postValidator;
    private final HistoryRecorder historyRecorder;
    private final PostNotificationService postNotificationService;


    /**
     * 프로젝트와 노드 일치 여부, 작성자 권한을 검증한 뒤 게시글을 수정한다.
     */
    public PostResponse update(Long projectId, Long nodeId, Long postId, Long userId, PostUpdateRequest request) {
        postValidator.validateNodeAndProject(nodeId, projectId);

        Post target = postService.findById(postId);
        postService.validateNode(target, nodeId);
        if (!target.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_POST_UPDATE);
        }

        historyRecorder.recordHistory(HistoryType.POST, postId, ActionType.UPDATE, PostHistorySnapshot.from(target));

        target.update(request);

        List<PostFile> updatedFiles = updateFiles(postId, request.files());
        List<PostFile> visibleFiles = updatedFiles.stream()
                .filter(file -> file.getDeletedAt() == null)
                .toList();

        List<PostLink> updatedLinks = updateLinks(postId, request.links());
        List<PostLink> visibleLinks = updatedLinks.stream()
                .filter(link -> link.getDeletedAt() == null)
                .toList();

        postNotificationService.notifyUpdated(projectId, target);

        return PostResponse.from(target, visibleFiles, visibleLinks);
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

    /**
     * 링크 수정 요청을 반영해 추가/삭제/내용 변경을 처리한다.
     *
     * @param postId 게시글 ID
     * @param linkRequests 링크 수정 요청 목록
     * @return 최신 링크 목록
     */
    private List<PostLink> updateLinks(Long postId, List<PostLinkUpdateRequest> linkRequests) {
        List<PostLink> existingLinks = postService.findLinksByPostId(postId);

        if (linkRequests == null || linkRequests.isEmpty()) {
            return existingLinks;
        }

        Map<Long, PostLink> existingLinkMap = mapExistingLinks(existingLinks);
        markRemovedLinks(existingLinks, extractLinkRequestedIds(linkRequests));

        List<PostLink> resultLinks = new ArrayList<>();
        for (PostLinkUpdateRequest req : linkRequests) {
            PostLink processed = handleLinkRequest(postId, req, existingLinkMap);
            if (processed != null) {
                resultLinks.add(processed);
            }
        }
        return resultLinks;
    }

    /** 기존 링크를 ID 기반 맵으로 구성한다. */
    private Map<Long, PostLink> mapExistingLinks(List<PostLink> existingLinks) {
        return existingLinks.stream()
                .collect(Collectors.toMap(PostLink::getLinkId, link -> link));
    }

    /** 요청에 포함된 링크 ID만 추출한다. */
    private Set<Long> extractLinkRequestedIds(List<PostLinkUpdateRequest> linkRequests) {
        return linkRequests.stream()
                .map(PostLinkUpdateRequest::linkId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /** 요청 목록에 없는 기존 링크를 삭제 처리한다. */
    private void markRemovedLinks(List<PostLink> existingLinks, Set<Long> requestedIds) {
        for (PostLink existingLink : existingLinks) {
            if (!requestedIds.contains(existingLink.getLinkId())) {
                existingLink.markDeleted();
            }
        }
    }

    /**
     * 단일 링크 수정 요청을 처리한다.
     */
    private PostLink handleLinkRequest(Long postId,
                                       PostLinkUpdateRequest req,
                                       Map<Long, PostLink> existingLinkMap) {
        if (req.linkId() == null) {
            return addNewLink(postId, req);
        }

        PostLink target = existingLinkMap.get(req.linkId());
        if (target == null) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_POST_LINK);
        }
        target.update(req.referenceLink(), req.linkDescription());
        return target;
    }

    /**
     * 신규 링크를 생성하고 저장한다.
     */
    private PostLink addNewLink(Long postId, PostLinkUpdateRequest req) {
        if (req.deleted()) {
            throw new BusinessException(ErrorCode.INVALID_POST_LINK_UPDATE);
        }
        PostLink newLink = PostLink.of(postId, req.referenceLink(), req.linkDescription());
        return postService.savePostLink(newLink);
    }
}
