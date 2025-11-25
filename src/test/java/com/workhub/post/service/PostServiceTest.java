package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.record.request.PostCreateRequest;
import com.workhub.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
public class PostServiceTest {
    @Mock
    PostRepository postRepository;
    @InjectMocks
    PostService postService;

    @Test
    @DisplayName("부모 게시물이 없으면 예외를 던진다.")
    void create_withParentNotFound_shouldThrow() {
        PostCreateRequest request = new PostCreateRequest(
                "title", PostType.NOTICE, "content", "11.1.1",1L, HashTag.DESIGN
        );
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("정상적으로 게시글을 생성하면 저장된 엔티티를 반환한다")
    void create_success_shouldReturnSavedPost() {
        Post saved = Post.builder()
                .id(10L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .postIp("127.0.0.1")
                .hashtag(HashTag.DESIGN)
                .build();
        given(postRepository.save(any(Post.class))).willReturn(saved);

        PostCreateRequest request = new PostCreateRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, HashTag.DESIGN
        );

        Post result = postService.create(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("title");
    }

}
