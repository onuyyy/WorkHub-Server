package com.workhub.cs.service.csQna;

import com.workhub.cs.dto.csQna.CsQnaResponse;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsQna;
import com.workhub.cs.service.CsPostAccessValidator;
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
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadCsQnaServiceTest {

    @Mock
    private CsQnaService csQnaService;

    @Mock
    private CsPostAccessValidator csPostAccessValidator;

    @InjectMocks
    private ReadCsQnaService readCsQnaService;

    @Test
    @DisplayName("댓글 목록을 계층 구조로 조회한다.")
    void givenCsPostId_whenFindCsQnas_thenReturnsHierarchicalComments() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());

        // 최상위 댓글
        List<CsQna> topLevelComments = List.of(
                CsQna.builder()
                        .csQnaId(10L)
                        .csPostId(csPostId)
                        .userId(3L)
                        .parentQnaId(null)
                        .qnaContent("첫 번째 댓글")
                        .build(),
                CsQna.builder()
                        .csQnaId(11L)
                        .csPostId(csPostId)
                        .userId(4L)
                        .parentQnaId(null)
                        .qnaContent("두 번째 댓글")
                        .build()
        );

        // 모든 댓글 (최상위 + 답글)
        List<CsQna> allComments = List.of(
                CsQna.builder().csQnaId(10L).csPostId(csPostId).userId(3L).parentQnaId(null).qnaContent("첫 번째 댓글").build(),
                CsQna.builder().csQnaId(20L).csPostId(csPostId).userId(5L).parentQnaId(10L).qnaContent("첫 번째 댓글의 답글 1").build(),
                CsQna.builder().csQnaId(21L).csPostId(csPostId).userId(6L).parentQnaId(10L).qnaContent("첫 번째 댓글의 답글 2").build(),
                CsQna.builder().csQnaId(11L).csPostId(csPostId).userId(4L).parentQnaId(null).qnaContent("두 번째 댓글").build()
        );

        Page<CsQna> mockPage = new PageImpl<>(topLevelComments, pageable, 2);

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId))
                .thenReturn(CsPost.builder().csPostId(csPostId).projectId(projectId).userId(1L).title("t").content("c").build());
        when(csQnaService.findAllByCsPostId(csPostId)).thenReturn(allComments);
        when(csQnaService.findCsQnasWithReplies(csPostId, pageable)).thenReturn(mockPage);

        Page<CsQnaResponse> result = readCsQnaService.findCsQnas(projectId, csPostId, pageable);

        verify(csPostAccessValidator).validateProjectAndGetPost(projectId, csPostId);
        verify(csQnaService).findAllByCsPostId(csPostId);
        verify(csQnaService).findCsQnasWithReplies(csPostId, pageable);

        // 페이징 검증
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);

        // 첫 번째 댓글 검증
        CsQnaResponse firstComment = result.getContent().get(0);
        assertThat(firstComment.csQnaId()).isEqualTo(10L);
        assertThat(firstComment.qnaContent()).isEqualTo("첫 번째 댓글");
        assertThat(firstComment.parentQnaId()).isNull();
        assertThat(firstComment.children()).hasSize(2);

        // 첫 번째 댓글의 답글 검증
        assertThat(firstComment.children().get(0).csQnaId()).isEqualTo(20L);
        assertThat(firstComment.children().get(0).qnaContent()).isEqualTo("첫 번째 댓글의 답글 1");
        assertThat(firstComment.children().get(0).parentQnaId()).isEqualTo(10L);

        assertThat(firstComment.children().get(1).csQnaId()).isEqualTo(21L);
        assertThat(firstComment.children().get(1).qnaContent()).isEqualTo("첫 번째 댓글의 답글 2");
        assertThat(firstComment.children().get(1).parentQnaId()).isEqualTo(10L);

        // 두 번째 댓글 검증
        CsQnaResponse secondComment = result.getContent().get(1);
        assertThat(secondComment.csQnaId()).isEqualTo(11L);
        assertThat(secondComment.qnaContent()).isEqualTo("두 번째 댓글");
        assertThat(secondComment.parentQnaId()).isNull();
        assertThat(secondComment.children()).isEmpty();
    }

    @Test
    @DisplayName("답글이 없는 댓글만 있을 때 정상 조회한다.")
    void givenTopLevelCommentsOnly_whenFindCsQnas_thenReturnsCommentsWithEmptyChildren() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Pageable pageable = PageRequest.of(0, 20);

        List<CsQna> topLevelComments = List.of(
                CsQna.builder().csQnaId(10L).csPostId(csPostId).userId(3L).parentQnaId(null).qnaContent("댓글 1").build(),
                CsQna.builder().csQnaId(11L).csPostId(csPostId).userId(4L).parentQnaId(null).qnaContent("댓글 2").build()
        );

        Page<CsQna> mockPage = new PageImpl<>(topLevelComments, pageable, 2);

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId))
                .thenReturn(CsPost.builder().csPostId(csPostId).projectId(projectId).userId(1L).title("t").content("c").build());
        when(csQnaService.findAllByCsPostId(csPostId)).thenReturn(topLevelComments);
        when(csQnaService.findCsQnasWithReplies(csPostId, pageable)).thenReturn(mockPage);

        Page<CsQnaResponse> result = readCsQnaService.findCsQnas(projectId, csPostId, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).children()).isEmpty();
        assertThat(result.getContent().get(1).children()).isEmpty();
    }

    @Test
    @DisplayName("빈 페이지를 반환한다.")
    void givenNoComments_whenFindCsQnas_thenReturnsEmptyPage() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Pageable pageable = PageRequest.of(0, 20);

        Page<CsQna> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId))
                .thenReturn(CsPost.builder().csPostId(csPostId).projectId(projectId).userId(1L).title("t").content("c").build());
        when(csQnaService.findAllByCsPostId(csPostId)).thenReturn(List.of());
        when(csQnaService.findCsQnasWithReplies(csPostId, pageable)).thenReturn(emptyPage);

        Page<CsQnaResponse> result = readCsQnaService.findCsQnas(projectId, csPostId, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("페이징 처리를 정확하게 수행한다.")
    void givenPageableWithCustomSize_whenFindCsQnas_thenReturnsPaginatedResult() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Pageable pageable = PageRequest.of(1, 5);

        List<CsQna> topLevelComments = List.of(
                CsQna.builder().csQnaId(6L).csPostId(csPostId).userId(1L).parentQnaId(null).qnaContent("6번째 댓글").build(),
                CsQna.builder().csQnaId(7L).csPostId(csPostId).userId(1L).parentQnaId(null).qnaContent("7번째 댓글").build(),
                CsQna.builder().csQnaId(8L).csPostId(csPostId).userId(1L).parentQnaId(null).qnaContent("8번째 댓글").build(),
                CsQna.builder().csQnaId(9L).csPostId(csPostId).userId(1L).parentQnaId(null).qnaContent("9번째 댓글").build(),
                CsQna.builder().csQnaId(10L).csPostId(csPostId).userId(1L).parentQnaId(null).qnaContent("10번째 댓글").build()
        );

        Page<CsQna> mockPage = new PageImpl<>(topLevelComments, pageable, 15);

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId))
                .thenReturn(CsPost.builder().csPostId(csPostId).projectId(projectId).userId(1L).title("t").content("c").build());
        when(csQnaService.findAllByCsPostId(csPostId)).thenReturn(topLevelComments);
        when(csQnaService.findCsQnasWithReplies(csPostId, pageable)).thenReturn(mockPage);

        Page<CsQnaResponse> result = readCsQnaService.findCsQnas(projectId, csPostId, pageable);

        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(15);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.hasPrevious()).isTrue();
    }

    @Test
    @DisplayName("중첩된 답글 구조를 정확하게 구성한다.")
    void givenNestedReplies_whenFindCsQnas_thenBuildsNestedStructure() {
        Long projectId = 1L;
        Long csPostId = 2L;
        Pageable pageable = PageRequest.of(0, 20);

        List<CsQna> topLevelComments = List.of(
                CsQna.builder().csQnaId(1L).csPostId(csPostId).userId(1L).parentQnaId(null).qnaContent("최상위 댓글").build()
        );

        // 중첩 구조: 최상위 댓글 → 답글 → 답글의 답글
        List<CsQna> allComments = List.of(
                CsQna.builder().csQnaId(1L).csPostId(csPostId).userId(1L).parentQnaId(null).qnaContent("최상위 댓글").build(),
                CsQna.builder().csQnaId(2L).csPostId(csPostId).userId(2L).parentQnaId(1L).qnaContent("1차 답글").build(),
                CsQna.builder().csQnaId(3L).csPostId(csPostId).userId(3L).parentQnaId(2L).qnaContent("2차 답글").build()
        );

        Page<CsQna> mockPage = new PageImpl<>(topLevelComments, pageable, 1);

        when(csPostAccessValidator.validateProjectAndGetPost(projectId, csPostId))
                .thenReturn(CsPost.builder().csPostId(csPostId).projectId(projectId).userId(1L).title("t").content("c").build());
        when(csQnaService.findAllByCsPostId(csPostId)).thenReturn(allComments);
        when(csQnaService.findCsQnasWithReplies(csPostId, pageable)).thenReturn(mockPage);

        Page<CsQnaResponse> result = readCsQnaService.findCsQnas(projectId, csPostId, pageable);

        assertThat(result.getContent()).hasSize(1);

        CsQnaResponse topLevel = result.getContent().get(0);
        assertThat(topLevel.csQnaId()).isEqualTo(1L);
        assertThat(topLevel.children()).hasSize(1);

        CsQnaResponse firstReply = topLevel.children().get(0);
        assertThat(firstReply.csQnaId()).isEqualTo(2L);
        assertThat(firstReply.children()).hasSize(1);

        CsQnaResponse secondReply = firstReply.children().get(0);
        assertThat(secondReply.csQnaId()).isEqualTo(3L);
        assertThat(secondReply.children()).isEmpty();
    }
}