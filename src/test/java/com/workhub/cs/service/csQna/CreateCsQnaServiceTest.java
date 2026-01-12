package com.workhub.cs.service.csQna;

import com.workhub.cs.dto.csQna.CsQnaRequest;
import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.global.port.AuthorLookupPort;
import com.workhub.global.port.dto.AuthorProfile;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCsQnaServiceTest {

    @Mock
    private CsQnaService csQnaService;

    @Mock
    private CsPostAccessValidator csPostAccessValidator;

    @Mock
    private CsQnaNotificationService csQnaNotificationService;

    @Mock
    private AuthorLookupPort authorLookupPort;

    @InjectMocks
    private CreateCsQnaService createCsQnaService;

    @Test
    @DisplayName("CS 댓글을 정상적으로 생성한다.")
    void givenValidRequest_whenCreate_thenReturnResponse() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long userId = 3L;

        CsQnaRequest request = new CsQnaRequest("답변드립니다.", null);

        CsQna saved = CsQna.builder()
                .csQnaId(10L)
                .csPostId(csPostId)
                .userId(userId)
                .qnaContent("답변드립니다.")
                .build();

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId))
                .thenReturn(CsPost.builder().csPostId(csPostId).projectId(projectId).userId(1L).title("t").content("c").build());
        when(csQnaService.save(any(CsQna.class))).thenReturn(saved);
        when(authorLookupPort.findByUserId(userId)).thenReturn(java.util.Optional.of(new AuthorProfile(userId, "author")));

        CsQnaResponse response = createCsQnaService.create(projectId, csPostId, userId, request);

        assertThat(response.csQnaId()).isEqualTo(saved.getCsQnaId());
        assertThat(response.qnaContent()).isEqualTo(saved.getQnaContent());
        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).save(any(CsQna.class));
    }

    @Test
    @DisplayName("댓글 내용이 비어있으면 예외를 던진다.")
    void givenBlankContent_whenCreate_thenThrow() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long userId = 3L;

        CsQnaRequest request = new CsQnaRequest(" ", null);

        assertThatThrownBy(() -> createCsQnaService.create(projectId, csPostId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CS_QNA_CONTENT);

        verifyNoInteractions(csPostAccessValidator);
        verify(csQnaService, never()).save(any(CsQna.class));
    }

    @Test
    @DisplayName("부모 댓글이 다른 게시글에 속하면 예외.")
    void givenParentFromDifferentPost_whenCreate_thenThrow() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long userId = 3L;
        Long parentId = 4L;

        CsQnaRequest request = new CsQnaRequest("답변", parentId);

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId))
                .thenReturn(CsPost.builder().csPostId(csPostId).projectId(projectId).userId(1L).title("t").content("c").build());

        CsQna parent = CsQna.builder()
                .csQnaId(parentId)
                .csPostId(999L)
                .userId(9L)
                .qnaContent("parent")
                .build();
        when(csQnaService.findById(parentId)).thenReturn(parent);

        assertThatThrownBy(() -> createCsQnaService.create(projectId, csPostId, userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_MATCHED_CS_QNA_POST);

        verify(csQnaService, never()).save(any(CsQna.class));
    }
}
