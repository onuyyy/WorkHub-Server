package com.workhub.cs.service.csQna;

import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.dto.csQna.CsQnaUpdateRequest;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
import com.workhub.global.port.AuthorLookupPort;
import com.workhub.global.port.dto.AuthorProfile;
import com.workhub.global.entity.ActionType;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCsQnaServiceTest {

    @Mock
    private CsQnaService csQnaService;

    @Mock
    private CsPostAccessValidator csPostAccessValidator;

    @Mock
    private AuthorLookupPort authorLookupPort;

    @InjectMocks
    private UpdateCsQnaService updateCsQnaService;

    @DisplayName("댓글 수정 시 프로젝트/게시글 검증과 소유자 검증이 모두 통과하면 컨텐츠가 갱신된다")
    @Test
    void updateCsQnaSuccess() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long csQnaId = 3L;
        Long userId = 4L;
        CsQna existing = CsQna.builder()
                .csQnaId(csQnaId)
                .csPostId(csPostId)
                .userId(userId)
                .qnaContent("기존 내용")
                .build();

        when(csQnaService.findByCsQnaAndMatchedUserId(csQnaId, userId))
                .thenReturn(existing);
        when(authorLookupPort.findByUserId(userId)).thenReturn(java.util.Optional.of(new AuthorProfile(userId, "author")));

        CsQnaUpdateRequest request = new CsQnaUpdateRequest("수정된 내용");

        CsQnaResponse response = updateCsQnaService.update(projectId, csPostId, csQnaId, userId, request);

        assertThat(response.qnaContent()).isEqualTo("수정된 내용");
        assertThat(existing.getQnaContent()).isEqualTo("수정된 내용");
        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).findByCsQnaAndMatchedUserId(csQnaId, userId);
        verify(csQnaService).snapShotAndRecordHistory(existing, csQnaId, ActionType.UPDATE);
    }

    @DisplayName("요청한 게시글과 댓글이 속한 게시글이 다르면 예외가 발생한다")
    @Test
    void updateCsQnaFailedByDifferentCsPost() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Long csQnaId = 3L;
        Long userId = 4L;
        CsQna existing = CsQna.builder()
                .csQnaId(csQnaId)
                .csPostId(99L)
                .userId(userId)
                .qnaContent("기존 내용")
                .build();

        when(csQnaService.findByCsQnaAndMatchedUserId(csQnaId, userId))
                .thenReturn(existing);

        CsQnaUpdateRequest request = new CsQnaUpdateRequest("수정된 내용");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> updateCsQnaService.update(projectId, csPostId, csQnaId, userId, request));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_MATCHED_CS_QNA_POST);
    }
}
