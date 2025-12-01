package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.PostType;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.request.PostUpdateRequest;
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

    /**
     * 게시글을 생성한다.
     *
     * @param request 게시글 생성 요청값
     * @return 저장된 게시글 엔티티
     * @throws BusinessException 부모 게시글이 존재하지 않을 때
     */
    @Transactional
    public Post create(PostRequest request){
        Long parentPostId = request.parentPostId();
        if (parentPostId != null && !postRepository.existsByPostIdAndDeletedAtIsNull(parentPostId)) {
            throw new BusinessException(ErrorCode.PARENT_POST_NOT_FOUND);
        }

        return postRepository.save(Post.of(parentPostId, request));
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
     * 게시글을 수정한다.
     *
     * @throws BusinessException 게시글이 존재하지 않을 때
     */
    @Transactional
    public Post update(Post target, PostUpdateRequest request){
        target.update(request);
        return target;
    }

    /**
     * 게시글을 삭제한다.
     *
     * @param postId 삭제할 게시글 식별자
     * @throws BusinessException 게시글이 존재하지 않을 때
     */
    @Transactional
    public void delete(Long postId){
        Post target = findById(postId);
        if (target.isDeleted()) {
            throw new BusinessException(ErrorCode.ALREADY_DELETED_POST);
        }
        target.markDeleted();
    }
}
