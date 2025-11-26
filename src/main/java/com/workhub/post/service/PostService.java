package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.entity.Post;
import com.workhub.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Post parent = request.parentPostId() == null
                ? null
                : postRepository.findById(request.parentPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        return postRepository.save(Post.of(parent, request));
    }

    @Transactional(readOnly = true)
    public List<Post> findAll(){
        return postRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
