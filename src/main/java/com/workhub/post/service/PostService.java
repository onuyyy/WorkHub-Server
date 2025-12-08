package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostFile;
import com.workhub.post.entity.PostLink;
import com.workhub.post.entity.PostType;
import com.workhub.post.repository.PostFileRepository;
import com.workhub.post.repository.PostLinkRepository;
import com.workhub.post.repository.PostRepository;
import com.workhub.post.repository.PostSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;
    private final PostLinkRepository postLinkRepository;

    /**
     * 게시글을 저장한다.
     *
     * @param post 저장할 게시글
     * @return 저장된 게시글
     */
    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }

    /**
     * 삭제되지 않은 게시글이 존재하는지 확인한다.
     *
     * @param postId 게시글 ID
     * @return 존재 여부
     */
    @Transactional(readOnly = true)
    public boolean existsActivePost(Long postId) {
        return postRepository.existsByPostIdAndDeletedAtIsNull(postId);
    }

    /**
     * 삭제되지 않은 게시글을 조회한다.
     *
     * @param id 게시글 ID
     * @return 게시글 엔티티
     * @throws BusinessException 게시글이 없을 경우
     */
    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findByPostIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    /**
     * 지정된 노드의 부모 게시글(답글이 아닌 글)만 검색한다.
     *
     * @param nodeId 프로젝트 노드 ID
     * @param keyword 검색 키워드
     * @param postType 게시글 타입
     * @param pageable 페이지 정보
     * @return 검색 결과 페이지
     */
    @Transactional(readOnly = true)
    public Page<Post> searchParentPosts(Long nodeId,
                                        String keyword,
                                        PostType postType,
                                        Pageable pageable) {
        Specification<Post> spec = Specification.where(PostSpecifications.withProjectNode(nodeId))
                .and(PostSpecifications.withKeyword(keyword))
                .and(PostSpecifications.withPostType(postType))
                .and(PostSpecifications.onlyRootPosts());

        return postRepository.findAll(spec, pageable);
    }

    /**
     * 여러 부모 게시글에 달린 자식 글을 한 번에 조회한다.
     *
     * @param parentIds 부모 게시글 ID 목록
     * @return 자식 게시글 목록, 없으면 빈 리스트
     */
    @Transactional(readOnly = true)
    public List<Post> findChildren(List<Long> parentIds) {
        if (parentIds == null || parentIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postRepository.findByParentPostIdInAndDeletedAtIsNull(parentIds);
    }

    /**
     * 단일 부모 게시글에 속한 자식 글 목록을 조회한다.
     *
     * @param parentId 부모 게시글 ID
     * @return 자식 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<Post> findChildren(Long parentId) {
        return postRepository.findByParentPostIdAndDeletedAtIsNull(parentId);
    }

    /**
     * 요청받은 프로젝트 노드와 게시글의 노드가 일치하는지 검증한다.
     *
     * @param post 게시글 엔티티
     * @param nodeId 요청 노드 ID
     * @throws BusinessException 노드가 다를 경우
     */
    public void validateNode(Post post, Long nodeId) {
        if (nodeId == null || !nodeId.equals(post.getProjectNodeId())) {
            throw new BusinessException(ErrorCode.NOT_MATCHED_PROJECT_POST);
        }
    }

    public List<PostFile> savePostFiles(List<PostFile> postFiles) {
        return postFileRepository.saveAll(postFiles);
    }

    public PostFile savePostFile(PostFile postFile) {
        return postFileRepository.save(postFile);
    }

    @Transactional(readOnly = true)
    public List<PostFile> findFilesByPostId(Long postId) {
        return postFileRepository.findByPostId(postId);
    }

    public List<PostLink> savePostLinks(List<PostLink> postLinks) {
        return postLinkRepository.saveAll(postLinks);
    }

    public PostLink savePostLink(PostLink postLink) {
        return postLinkRepository.save(postLink);
    }

    @Transactional(readOnly = true)
    public List<PostLink> findLinksByPostId(Long postId) {
        return postLinkRepository.findByPostId(postId);
    }
}
