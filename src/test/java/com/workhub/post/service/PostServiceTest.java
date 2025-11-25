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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostService postService;

    @Test
    @DisplayName("부모 게시글이 존재하지 않으면 예외를 던진다")
    void create_parentNotFound_shouldThrow() {
        // given
        PostCreateRequest request = new PostCreateRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", 1L, HashTag.DESIGN
        );
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글을 생성하면 저장된 엔티티를 반환한다")
    void create_success_shouldReturnSavedPost() {
        // given
        Post saved = Post.builder()
                .id(10L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .postIp("127.0.0.1")
                .build();
        given(postRepository.save(any(Post.class))).willReturn(saved);

        PostCreateRequest request = new PostCreateRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, HashTag.DESIGN
        );

        // when
        Post result = postService.create(request);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("title");
    }
}
