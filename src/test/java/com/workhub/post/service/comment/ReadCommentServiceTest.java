package com.workhub.post.service.comment;

import com.workhub.post.dto.comment.response.CommentResponse;
import com.workhub.post.entity.PostComment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReadCommentServiceTest {

    @Mock
    CommentService commentService;

    @InjectMocks
    ReadCommentService readCommentService;

    @Test
    @DisplayName("최상위 댓글과 자식 댓글을 계층 구조로 반환한다")
    void findComment_buildHierarchy() {
        PageRequest pageable = PageRequest.of(0, 10);
        PostComment parent = comment(1L, null);
        PostComment child1 = comment(2L, 1L);
        PostComment child2 = comment(3L, 1L);

        given(commentService.findPostWithReplies(anyLong(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(parent), pageable, 1));
        given(commentService.findAllByPostId(anyLong()))
                .willReturn(List.of(parent, child1, child2));

        Page<CommentResponse> result = readCommentService.findComment(10L, 20L, pageable);

        assertThat(result.getContent()).hasSize(1);
        CommentResponse parentRes = result.getContent().get(0);
        assertThat(parentRes.children()).hasSize(2);
    }

    private PostComment comment(Long id, Long parentId) {
        return PostComment.builder()
                .commentId(id)
                .postId(20L)
                .userId(3L)
                .parentCommentId(parentId)
                .content("c" + id)
                .build();
    }
}
