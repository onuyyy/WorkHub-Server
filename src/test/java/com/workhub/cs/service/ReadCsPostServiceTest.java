package com.workhub.cs.service;

import com.workhub.cs.dto.CsPostResponse;
import com.workhub.cs.dto.CsPostSearchRequest;
import com.workhub.cs.entity.CsPost;
import com.workhub.cs.entity.CsPostStatus;
import com.workhub.global.error.ErrorCode;
import com.workhub.global.error.exception.BusinessException;
import com.workhub.project.entity.Project;
import com.workhub.project.entity.Status;
import com.workhub.project.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadCsPostServiceTest {

    @Mock
    private CsPostService csPostService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ReadCsPostService readCsPostService;

    private CsPost mockSaved;

    @BeforeEach
    void init() {
        mockSaved = CsPost.builder()
                .csPostId(1L)
                .projectId(1L)
                .userId(2L)
                .title("문의 제목")
                .content("문의 내용")
                .csPostStatus(CsPostStatus.RECEIVED)
                .build();
    }

    @Test
    @DisplayName("CS POST 게시글 상세 조회한다.")
    void givenCsPostId_whenGetCsPost_thenSuccess() {
        Long csPostId = 1L;
        Long projectId = 1L;

        when(projectService.validateCompletedProject(projectId)).thenReturn(
                Project.builder()
                        .projectId(projectId)
                        .projectTitle("project")
                        .status(Status.COMPLETED)
                        .build()
        );
        when(csPostService.findById(csPostId)).thenReturn(mockSaved);
        when(csPostService.findFilesByCsPostId(csPostId)).thenReturn(List.of());

        CsPostResponse response = readCsPostService.findCsPost(projectId, csPostId);

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(csPostId);
        verify(csPostService).findFilesByCsPostId(csPostId);
        assertThat(response.content()).isEqualTo("문의 내용");
        assertThat(response.title()).isEqualTo("문의 제목");
    }

    @Test
    @DisplayName("다른 프로젝트의 CS POST 조회 시 예외 발생")
    void givenWrongProjectId_whenGetCsPost_thenThrow() {
        Long csPostId = 1L;
        Long projectId = 999L;

        when(projectService.validateCompletedProject(projectId)).thenReturn(
                Project.builder()
                        .projectId(projectId)
                        .projectTitle("project")
                        .status(Status.COMPLETED)
                        .build()
        );
        CsPost otherProjectPost = CsPost.builder()
                .csPostId(csPostId)
                .projectId(1L)
                .userId(1L)
                .title("문의 제목")
                .content("문의 내용")
                .csPostStatus(CsPostStatus.RECEIVED)
                .build();
        when(csPostService.findById(csPostId)).thenReturn(otherProjectPost);

        assertThatThrownBy(() -> readCsPostService.findCsPost(projectId, csPostId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.NOT_MATCHED_PROJECT_CS_POST.getMessage());

        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findById(csPostId);
        verify(csPostService, never()).findFilesByCsPostId(csPostId);
    }

    @Test
    @DisplayName("CS POST 리스트를 조회한다.")
    void givenProjectIdAndSearchRequest_whenFindCsPosts_thenReturnsPagedResult() {
        // given
        Long projectId = 1L;
        CsPostSearchRequest request = CsPostSearchRequest.builder().csPostStatus(CsPostStatus.RECEIVED).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

        List<CsPost> content = List.of(
                CsPost.builder().csPostId(1L).title("t1").projectId(1L).build(),
                CsPost.builder().csPostId(2L).title("t2").projectId(1L).build()
        );

        Page<CsPost> mockPage =
                new PageImpl<>(content, pageable, 2);

        when(projectService.validateCompletedProject(projectId)).thenReturn(
                Project.builder()
                        .projectId(projectId)
                        .projectTitle("project")
                        .status(Status.COMPLETED)
                        .build()
        );
        when(csPostService.findCsPosts(request, pageable)).thenReturn(mockPage);

        // when
        Page<CsPostResponse> result = readCsPostService.findCsPosts(projectId, request, pageable);

        // then
        verify(projectService).validateCompletedProject(projectId);
        verify(csPostService).findCsPosts(request, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        // DTO 매핑 검증
        assertThat(result.getContent().get(0).title()).isEqualTo("t1");

    }
}
