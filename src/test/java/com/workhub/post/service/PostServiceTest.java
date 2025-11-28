package com.workhub.post.service;

import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.post.entity.HashTag;
import com.workhub.post.entity.Post;
import com.workhub.post.entity.PostType;
import com.workhub.post.record.request.PostRequest;
import com.workhub.post.record.request.PostUpdateRequest;
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
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class PostServiceTest {
    @Mock
    PostRepository postRepository;
    @InjectMocks
    PostService postService;

    @Test
    @DisplayName("부모 게시물이 없으면 예외를 던진다.")
    void create_withParentNotFound_shouldThrow() {
        PostRequest request = new PostRequest(
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
                .postId(10L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .postIp("127.0.0.1")
                .hashtag(HashTag.DESIGN)
                .build();
        given(postRepository.save(any(Post.class))).willReturn(saved);

        PostRequest request = new PostRequest(
                "title", PostType.NOTICE, "content", "127.0.0.1", null, HashTag.DESIGN
        );

        Post result = postService.create(request);

        assertThat(result.getPostId()).isEqualTo(10L);
        assertThat(result.getTitle()).isEqualTo("title");
    }

    @Test
    @DisplayName("수정 대상 게시글이 없으면 예외를 던진다")
    void update_withPostNotFound_shouldThrow() {
        given(postRepository.findById(99L)).willReturn(Optional.empty());

        PostUpdateRequest request = new PostUpdateRequest(
                "edited title", PostType.NOTICE, "edited content", "10.0.0.1", HashTag.DESIGN
        );

        assertThatThrownBy(() -> postService.update(99L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글을 수정하면 필드가 갱신된다")
    void update_success_shouldChangeFields() {
        Post origin = Post.builder()
                .postId(1L)
                .title("old")
                .content("old")
                .type(PostType.GENERAL)
                .postIp("1.1.1.1")
                .hashtag(HashTag.REQ_DEF)
                .build();
        given(postRepository.findById(1L)).willReturn(Optional.of(origin));

        PostUpdateRequest request = new PostUpdateRequest(
                "new", PostType.NOTICE, "new content", "2.2.2.2", HashTag.DESIGN
        );

        Post result = postService.update(1L, request);

        assertThat(result.getTitle()).isEqualTo("new");
        assertThat(result.getPostIp()).isEqualTo("2.2.2.2");
    }

    @Test
    @DisplayName("삭제 대상 게시글이 없으면 예외를 던진다")
    void delete_withPostNotFound_shouldThrow() {
        given(postRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> postService.delete(99L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 삭제 성공 시 repository.delete가 호출된다")
    void delete_success_shouldInvokeDelete() {
        Post existing = Post.builder()
                .postId(1L)
                .title("title")
                .content("content")
                .type(PostType.NOTICE)
                .build();
        given(postRepository.findById(1L)).willReturn(Optional.of(existing));

        postService.delete(1L);

        // 삭제 로직이 repository.delete 호출까지 수행했는지 검증한다.
        verify(postRepository).delete(existing);
    }



}
