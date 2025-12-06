package com.workhub.cs.service.csQna;

import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.global.history.HistoryRecorder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteCsQnaServiceTest {

    @Mock
    private CsQnaService csQnaService;

    @Mock
    private CsPostAccessValidator csPostAccessValidator;

    @Mock
    private HistoryRecorder historyRecorder;

    @InjectMocks
    private DeleteCsQnaService deleteCsQnaService;

    @Test
    @DisplayName("답글이 없는 댓글을 삭제한다.")
    void givenCommentWithNoReplies_whenDelete_thenDeletesOnlyComment() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long csQnaId = 10L;
        Long userId = 100L;

        CsPost csPost = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .userId(userId)
                .title("title")
                .content("content")
                .build();

        CsQna csQna = CsQna.builder()
                .csQnaId(csQnaId)
                .csPostId(csPostId)
                .userId(userId)
                .parentQnaId(null)
                .qnaContent("댓글 내용")
                .build();

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId)).thenReturn(csPost);
        when(csQnaService.findByCsQnaAndMatchedUserId(csQnaId, userId)).thenReturn(csQna);
        when(csQnaService.findByParentQnaId(csQnaId)).thenReturn(List.of());

        Long result = deleteCsQnaService.delete(projectId, csPostId, csQnaId, userId);

        assertThat(result).isEqualTo(csQnaId);
        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).findByCsQnaAndMatchedUserId(csQnaId, userId);
        verify(csQnaService).findByParentQnaId(csQnaId);
        assertThat(csQna.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("답글이 있는 댓글을 삭제하면 모든 자식 댓글도 함께 삭제된다.")
    void givenCommentWithReplies_whenDelete_thenDeletesCommentAndAllReplies() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long parentId = 10L;
        Long userId = 100L;

        CsPost csPost = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .userId(userId)
                .title("title")
                .content("content")
                .build();

        CsQna parentComment = CsQna.builder()
                .csQnaId(parentId)
                .csPostId(csPostId)
                .userId(userId)
                .parentQnaId(null)
                .qnaContent("부모 댓글")
                .build();

        CsQna childComment1 = CsQna.builder()
                .csQnaId(20L)
                .csPostId(csPostId)
                .userId(userId)
                .parentQnaId(parentId)
                .qnaContent("자식 댓글 1")
                .build();

        CsQna childComment2 = CsQna.builder()
                .csQnaId(21L)
                .csPostId(csPostId)
                .userId(userId)
                .parentQnaId(parentId)
                .qnaContent("자식 댓글 2")
                .build();

        CsQna grandchildComment = CsQna.builder()
                .csQnaId(30L)
                .csPostId(csPostId)
                .userId(userId)
                .parentQnaId(20L)
                .qnaContent("손자 댓글")
                .build();

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId)).thenReturn(csPost);
        when(csQnaService.findByCsQnaAndMatchedUserId(parentId, userId)).thenReturn(parentComment);
        when(csQnaService.findByParentQnaId(parentId)).thenReturn(List.of(childComment1, childComment2));
        when(csQnaService.findByParentQnaId(20L)).thenReturn(List.of(grandchildComment));
        when(csQnaService.findByParentQnaId(21L)).thenReturn(List.of());
        when(csQnaService.findByParentQnaId(30L)).thenReturn(List.of());

        Long result = deleteCsQnaService.delete(projectId, csPostId, parentId, userId);

        assertThat(result).isEqualTo(parentId);
        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).findByCsQnaAndMatchedUserId(parentId, userId);
        verify(csQnaService).findByParentQnaId(parentId);
        verify(csQnaService).findByParentQnaId(20L);
        verify(csQnaService).findByParentQnaId(21L);
        verify(csQnaService).findByParentQnaId(30L);

        assertThat(parentComment.isDeleted()).isTrue();
        assertThat(childComment1.isDeleted()).isTrue();
        assertThat(childComment2.isDeleted()).isTrue();
        assertThat(grandchildComment.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("이미 삭제된 댓글을 삭제하려고 하면 예외가 발생한다.")
    void givenAlreadyDeletedComment_whenDelete_thenThrowsException() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long csQnaId = 10L;
        Long userId = 100L;

        CsPost csPost = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .userId(userId)
                .title("title")
                .content("content")
                .build();

        CsQna deletedComment = CsQna.builder()
                .csQnaId(csQnaId)
                .csPostId(csPostId)
                .userId(userId)
                .parentQnaId(null)
                .qnaContent("삭제된 댓글")
                .build();
        deletedComment.markDeleted();

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId)).thenReturn(csPost);
        when(csQnaService.findByCsQnaAndMatchedUserId(csQnaId, userId)).thenReturn(deletedComment);

        assertThatThrownBy(() -> deleteCsQnaService.delete(projectId, csPostId, csQnaId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_DELETED_CS_QNA);

        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).findByCsQnaAndMatchedUserId(csQnaId, userId);
        verify(csQnaService, never()).findByParentQnaId(any());
    }

    @Test
    @DisplayName("다른 사용자의 댓글을 삭제하려고 하면 예외가 발생한다.")
    void givenDifferentUser_whenDelete_thenThrowsException() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long csQnaId = 10L;
        Long ownerId = 100L;
        Long deleterId = 999L;

        CsPost csPost = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .userId(ownerId)
                .title("title")
                .content("content")
                .build();

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId)).thenReturn(csPost);
        when(csQnaService.findByCsQnaAndMatchedUserId(csQnaId, deleterId))
                .thenThrow(new BusinessException(ErrorCode.NOT_MATCHED_CS_QNA_USERID));

        assertThatThrownBy(() -> deleteCsQnaService.delete(projectId, csPostId, csQnaId, deleterId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_CS_QNA_USERID);

        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).findByCsQnaAndMatchedUserId(csQnaId, deleterId);
        verify(csQnaService, never()).findByParentQnaId(any());
    }

    @Test
    @DisplayName("다른 게시글의 댓글을 삭제하려고 하면 예외가 발생한다.")
    void givenCommentFromDifferentPost_whenDelete_thenThrowsException() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long wrongPostId = 999L;
        Long csQnaId = 10L;
        Long userId = 100L;

        CsPost csPost = CsPost.builder()
                .csPostId(csPostId)
                .projectId(projectId)
                .userId(userId)
                .title("title")
                .content("content")
                .build();

        CsQna csQna = CsQna.builder()
                .csQnaId(csQnaId)
                .csPostId(wrongPostId)
                .userId(userId)
                .parentQnaId(null)
                .qnaContent("다른 게시글의 댓글")
                .build();

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId)).thenReturn(csPost);
        when(csQnaService.findByCsQnaAndMatchedUserId(csQnaId, userId)).thenReturn(csQna);

        assertThatThrownBy(() -> deleteCsQnaService.delete(projectId, csPostId, csQnaId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_CS_QNA_POST);

        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).findByCsQnaAndMatchedUserId(csQnaId, userId);
        verify(csQnaService, never()).findByParentQnaId(any());
    }
}
