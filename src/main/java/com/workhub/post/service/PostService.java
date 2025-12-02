package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.repository.PostRepository;
import com.workhub.post.repository.PostSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public boolean existsActivePost(Long postId) {
        return postRepository.existsByPostIdAndDeletedAtIsNull(postId);
    }

    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findByPostIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<Post> search(Long nodeId,
                             String keyword,
                             PostType postType,
                             HashTag hashTag,
                             Pageable pageable) {
        /** Specification 조합으로 프로젝트/검색 조건을 모두 반영한다. */
        Specification<Post> spec = Specification.where(PostSpecifications.withProjectNode(nodeId))
                .and(PostSpecifications.withKeyword(keyword))
                .and(PostSpecifications.withPostType(postType))
                .and(PostSpecifications.withHashTag(hashTag));

        return postRepository.findAll(spec, pageable);
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

}
