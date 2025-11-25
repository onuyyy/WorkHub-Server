package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.record.request.PostCreateRequest;
import com.workhub.post.entity.Post;
import com.workhub.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public Post create(PostCreateRequest request){
        Post parent = request.parentPostId() == null
                ? null
                : postRepository.findById(request.parentPostId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .type(request.postType())
                .postIp(request.postIp())
                .parentPostId(parent)
                .hashtag(request.hashTag())
                .build();
        return postRepository.save(post);
    }

    public List<Post> findAll(){
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }
}
